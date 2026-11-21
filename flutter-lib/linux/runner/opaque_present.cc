// The engine renders with EGL, GDK presents with GLX on X11, and the two cannot share textures, so
// the engine uploads each frame into an RGBA texture of GDK's context and passes it to
// gdk_cairo_draw_from_gl. A texture with alpha makes GDK blend it over the window's cairo surface:
// it first reads that surface back from the X server (XShmGetImage) and uploads it, every frame,
// about 5 ms of the GTK thread waiting on the server at 1024x768. A window Flutter paints whole has
// nothing to show through, so the frame is copied on the GPU into an RGB texture and GDK draws it
// as it is.
//
// The engine calls gdk_cairo_draw_from_gl through its own GOT, and libflutter_linux_gtk.so is
// loaded in a dlopen group of its own, so a definition here would not interpose: the GOT entry is
// pointed at DrawOpaque instead (engine_got.h).

#include "opaque_present.h"

#include "engine_got.h"

#include <dlfcn.h>
#include <gdk/gdkx.h>
#include <gtk/gtk.h>

#include <cstdlib>
#include <cstring>
#include <type_traits>

namespace {

typedef unsigned int GLenum;
typedef int GLint;
typedef unsigned int GLuint;
typedef int GLsizei;
typedef unsigned char GLubyte;

constexpr GLenum kGlTexture2d = 0x0DE1;
constexpr GLenum kGlTextureWidth = 0x1000;
constexpr GLenum kGlTextureHeight = 0x1001;
constexpr GLenum kGlTextureBinding2d = 0x8069;
constexpr GLenum kGlFramebuffer = 0x8D40;
constexpr GLenum kGlFramebufferBinding = 0x8CA6;
constexpr GLenum kGlColorAttachment0 = 0x8CE0;
constexpr GLenum kGlRgb8 = 0x8051;
// gdk_cairo_draw_from_gl's source_type for a texture (GL_TEXTURE).
constexpr int kGlTexture = 0x1702;

struct Gl {
  void (*GenFramebuffers)(GLsizei, GLuint *);
  void (*BindFramebuffer)(GLenum, GLuint);
  void (*FramebufferTexture2D)(GLenum, GLenum, GLenum, GLuint, GLint);
  void (*GetIntegerv)(GLenum, GLint *);
  void (*BindTexture)(GLenum, GLuint);
  void (*GetTexLevelParameteriv)(GLenum, GLint, GLenum, GLint *);
  void (*GenTextures)(GLsizei, GLuint *);
  void (*CopyTexImage2D)(GLenum, GLint, GLenum, GLint, GLint, GLsizei, GLsizei, GLint);
};
Gl g_gl;

// What one GDK GL context copies into: framebuffers are per context, so each context has its own.
struct Copy {
  GLuint framebuffer = 0;
  GLuint texture = 0;
};

GQuark CopyQuark() {
  static GQuark quark = g_quark_from_static_string("dev.equo.swt.opaque-present");
  return quark;
}

bool LoadGl() {
  void *gl = dlopen("libGL.so.1", RTLD_NOW | RTLD_LOCAL);
  if (gl == nullptr) return false;
  auto proc = reinterpret_cast<void *(*)(const GLubyte *)>(dlsym(gl, "glXGetProcAddressARB"));
  if (proc == nullptr) return false;
  auto load = [&](auto &fn, const char *name) {
    fn = reinterpret_cast<std::remove_reference_t<decltype(fn)>>(proc(reinterpret_cast<const GLubyte *>(name)));
    return fn != nullptr;
  };
  return load(g_gl.GenFramebuffers, "glGenFramebuffers") && load(g_gl.BindFramebuffer, "glBindFramebuffer") &&
         load(g_gl.FramebufferTexture2D, "glFramebufferTexture2D") && load(g_gl.GetIntegerv, "glGetIntegerv") &&
         load(g_gl.BindTexture, "glBindTexture") && load(g_gl.GetTexLevelParameteriv, "glGetTexLevelParameteriv") &&
         load(g_gl.GenTextures, "glGenTextures") && load(g_gl.CopyTexImage2D, "glCopyTexImage2D");
}

void DrawOpaque(cairo_t *cr, GdkWindow *window, int source, int source_type, int buffer_scale, int x, int y,
                int width, int height) {
  GdkGLContext *context = gdk_gl_context_get_current();
  if (source_type != kGlTexture || context == nullptr) {
    gdk_cairo_draw_from_gl(cr, window, source, source_type, buffer_scale, x, y, width, height);
    return;
  }
  auto *copy = static_cast<Copy *>(g_object_get_qdata(G_OBJECT(context), CopyQuark()));
  if (copy == nullptr) {
    copy = new Copy();
    g_object_set_qdata_full(G_OBJECT(context), CopyQuark(), copy, [](gpointer p) { delete static_cast<Copy *>(p); });
    g_gl.GenFramebuffers(1, &copy->framebuffer);
    g_gl.GenTextures(1, &copy->texture);
  }
  GLint saved_framebuffer = 0, saved_texture = 0, texture_width = 0, texture_height = 0;
  g_gl.GetIntegerv(kGlFramebufferBinding, &saved_framebuffer);
  g_gl.GetIntegerv(kGlTextureBinding2d, &saved_texture);
  g_gl.BindTexture(kGlTexture2d, static_cast<GLuint>(source));
  g_gl.GetTexLevelParameteriv(kGlTexture2d, 0, kGlTextureWidth, &texture_width);
  g_gl.GetTexLevelParameteriv(kGlTexture2d, 0, kGlTextureHeight, &texture_height);
  g_gl.BindFramebuffer(kGlFramebuffer, copy->framebuffer);
  g_gl.FramebufferTexture2D(kGlFramebuffer, kGlColorAttachment0, kGlTexture2d, static_cast<GLuint>(source), 0);
  g_gl.BindTexture(kGlTexture2d, copy->texture);
  g_gl.CopyTexImage2D(kGlTexture2d, 0, kGlRgb8, 0, 0, texture_width, texture_height, 0);
  g_gl.FramebufferTexture2D(kGlFramebuffer, kGlColorAttachment0, kGlTexture2d, 0, 0);
  g_gl.BindFramebuffer(kGlFramebuffer, static_cast<GLuint>(saved_framebuffer));
  g_gl.BindTexture(kGlTexture2d, static_cast<GLuint>(saved_texture));
  gdk_cairo_draw_from_gl(cr, window, static_cast<int>(copy->texture), kGlTexture, buffer_scale, x, y, width, height);
}

} // namespace

bool OpaquePresentInstall() {
  static int installed = -1;
  if (installed >= 0) return installed == 1;
  installed = 0;
  const char *off = getenv("EQUO_SWT_OPAQUE_PRESENT");
  if (off != nullptr && strcmp(off, "0") == 0) return false;
  GdkDisplay *display = gdk_display_get_default();
  if (display == nullptr || !GDK_IS_X11_DISPLAY(display) || !LoadGl()) return false;
  if (EngineGotPatch("gdk_cairo_draw_from_gl", reinterpret_cast<void *>(&DrawOpaque)) == 0) {
    g_warning("gdk_cairo_draw_from_gl not found in the engine's relocations; frames present as before");
    return false;
  }
  installed = 1;
  return true;
}
