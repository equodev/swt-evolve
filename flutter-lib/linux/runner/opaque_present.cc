// The engine renders with EGL, GDK presents with GLX on X11, and the two cannot share textures, so
// for each frame the engine reads the pixels back (raster thread), uploads them into a new RGBA
// texture of GDK's context (GTK thread) and passes that to gdk_cairo_draw_from_gl. A texture with
// alpha makes GDK blend it over the window's cairo surface: it first reads that surface back from
// the X server (XShmGetImage) and uploads it, every frame, about 5 ms of the GTK thread waiting on
// the server at 1024x768. A window Flutter paints whole has nothing to show through, so GDK is given
// an RGB texture instead, which it draws as it is.
//
// The pixels go straight into that RGB texture: the engine's upload (its glTexImage2D, through
// libepoxy) is held back and made into the RGB texture when the frame is drawn. Copying the
// uploaded texture on the GPU instead held the GTK thread until the upload had reached the GPU,
// ~2 ms a frame, and 15-25 ms while the GPU idles at its lowest clock; the upload itself returns
// once the driver has the pixels. When an upload was not held back (another caller, or the hook is
// not in place), the texture given is copied on the GPU.
//
// The engine calls gdk_cairo_draw_from_gl, and reaches epoxy's glTexImage2D, through its own GOT,
// and libflutter_linux_gtk.so is loaded in a dlopen group of its own, so a definition here would not
// interpose: the GOT entries are pointed here instead (engine_got.h).

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
constexpr GLenum kGlFramebufferBinding = 0x8CA6;
constexpr GLenum kGlColorAttachment0 = 0x8CE0;
constexpr GLenum kGlRgb8 = 0x8051;
constexpr GLenum kGlRgb = 0x1907;
constexpr GLenum kGlRgba = 0x1908;
constexpr GLenum kGlReadFramebuffer = 0x8CA8;
constexpr GLenum kGlDrawFramebuffer = 0x8CA9;
constexpr GLenum kGlReadFramebufferBinding = 0x8CAA;
constexpr GLenum kGlScissorTest = 0x0C11;
constexpr unsigned kGlColorBufferBit = 0x4000;
constexpr GLenum kGlNearest = 0x2600;
constexpr GLenum kGlUnsignedByte = 0x1401;
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
  void (*TexImage2D)(GLenum, GLint, GLint, GLsizei, GLsizei, GLint, GLenum, GLenum, const void *);
  void (*TexSubImage2D)(GLenum, GLint, GLint, GLint, GLsizei, GLsizei, GLenum, GLenum, const void *);
  void (*BlitFramebuffer)(GLint, GLint, GLint, GLint, GLint, GLint, GLint, GLint, unsigned, GLenum);
  unsigned char (*IsEnabled)(GLenum);
  void (*Enable)(GLenum);
  void (*Disable)(GLenum);
};
Gl g_gl;

// A frame upload held back: the texture the engine meant it for, in which context, and its pixels
// (the engine's buffer, which it leaves alone until its draw of the frame returns).
struct Held {
  GLuint texture = 0;
  GdkGLContext *context = nullptr;
  const void *pixels = nullptr;
  GLsizei width = 0, height = 0;
};
Held g_held;

typedef void (*TexImage2DFn)(GLenum, GLint, GLint, GLsizei, GLsizei, GLint, GLenum, GLenum, const void *);
// epoxy's own pointer for glTexImage2D: every upload not held back goes through it.
TexImage2DFn *g_epoxy_tex_image = nullptr;

// The engine's glTexImage2D. A whole frame of RGBA pixels, uploaded on the GTK thread into the
// texture bound in a GDK context, is held back for DrawOpaque.
void EngineTexImage2D(GLenum target, GLint level, GLint internal, GLsizei width, GLsizei height, GLint border,
                      GLenum format, GLenum type, const void *pixels) {
  GdkGLContext *context = pixels != nullptr ? gdk_gl_context_get_current() : nullptr;
  if (context != nullptr && target == kGlTexture2d && level == 0 && internal == static_cast<GLint>(kGlRgba) &&
      format == kGlRgba && type == kGlUnsignedByte && border == 0 && width > 0 && height > 0 &&
      g_main_context_is_owner(g_main_context_default())) {
    GLint bound = 0;
    g_gl.GetIntegerv(kGlTextureBinding2d, &bound);
    if (bound != 0) {
      g_held = {static_cast<GLuint>(bound), context, pixels, width, height};
      return;
    }
  }
  (*g_epoxy_tex_image)(target, level, internal, width, height, border, format, type, pixels);
}
// What the engine's GOT entry for epoxy_glTexImage2D is pointed at: a pointer, as epoxy's is.
TexImage2DFn g_engine_tex_image = EngineTexImage2D;

// The RGB textures of one GDK GL context (framebuffers are per context, so each context has its
// own): two, allocated once per frame size and taken in turn, as the one GDK drew last time may
// still be read by that draw, made in another context. For a copy, each is the colour attachment of
// a framebuffer of its own.
struct Copy {
  GLuint framebuffer = 0;
  GLuint targets[2] = {0, 0};
  GLuint textures[2] = {0, 0};
  GLint width[2] = {0, 0}, height[2] = {0, 0};
  bool attached[2] = {false, false};
  int next = 0;
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
         load(g_gl.GenTextures, "glGenTextures") && load(g_gl.TexImage2D, "glTexImage2D") &&
         load(g_gl.TexSubImage2D, "glTexSubImage2D") && load(g_gl.BlitFramebuffer, "glBlitFramebuffer") &&
         load(g_gl.IsEnabled, "glIsEnabled") && load(g_gl.Enable, "glEnable") && load(g_gl.Disable, "glDisable");
}

// Gives texture |i| of |copy| storage of the given size, once per size.
void Size(Copy *copy, int i, GLint width, GLint height) {
  if (copy->width[i] == width && copy->height[i] == height) return;
  g_gl.BindTexture(kGlTexture2d, copy->textures[i]);
  g_gl.TexImage2D(kGlTexture2d, 0, kGlRgb8, width, height, 0, kGlRgb, kGlUnsignedByte, nullptr);
  copy->width[i] = width;
  copy->height[i] = height;
  copy->attached[i] = false;
}

// Uploads the held pixels into texture |i|.
void Upload(Copy *copy, int i, const Held &held) {
  GLint saved_texture = 0;
  g_gl.GetIntegerv(kGlTextureBinding2d, &saved_texture);
  Size(copy, i, held.width, held.height);
  g_gl.BindTexture(kGlTexture2d, copy->textures[i]);
  g_gl.TexSubImage2D(kGlTexture2d, 0, 0, 0, held.width, held.height, kGlRgba, kGlUnsignedByte, held.pixels);
  g_gl.BindTexture(kGlTexture2d, static_cast<GLuint>(saved_texture));
}

// Copies texture |source| into texture |i| on the GPU.
void Blit(Copy *copy, int i, GLuint source) {
  GLint saved_draw = 0, saved_read = 0, saved_texture = 0, width = 0, height = 0;
  g_gl.GetIntegerv(kGlFramebufferBinding, &saved_draw);
  g_gl.GetIntegerv(kGlReadFramebufferBinding, &saved_read);
  g_gl.GetIntegerv(kGlTextureBinding2d, &saved_texture);
  g_gl.BindTexture(kGlTexture2d, source);
  g_gl.GetTexLevelParameteriv(kGlTexture2d, 0, kGlTextureWidth, &width);
  g_gl.GetTexLevelParameteriv(kGlTexture2d, 0, kGlTextureHeight, &height);
  Size(copy, i, width, height);
  if (!copy->attached[i]) {
    g_gl.BindFramebuffer(kGlDrawFramebuffer, copy->targets[i]);
    g_gl.FramebufferTexture2D(kGlDrawFramebuffer, kGlColorAttachment0, kGlTexture2d, copy->textures[i], 0);
    copy->attached[i] = true;
  }
  g_gl.BindFramebuffer(kGlReadFramebuffer, copy->framebuffer);
  g_gl.FramebufferTexture2D(kGlReadFramebuffer, kGlColorAttachment0, kGlTexture2d, source, 0);
  g_gl.BindFramebuffer(kGlDrawFramebuffer, copy->targets[i]);
  bool scissor = g_gl.IsEnabled(kGlScissorTest);
  if (scissor) g_gl.Disable(kGlScissorTest);
  g_gl.BlitFramebuffer(0, 0, width, height, 0, 0, width, height, kGlColorBufferBit, kGlNearest);
  if (scissor) g_gl.Enable(kGlScissorTest);
  g_gl.FramebufferTexture2D(kGlReadFramebuffer, kGlColorAttachment0, kGlTexture2d, 0, 0);
  g_gl.BindFramebuffer(kGlReadFramebuffer, static_cast<GLuint>(saved_read));
  g_gl.BindFramebuffer(kGlDrawFramebuffer, static_cast<GLuint>(saved_draw));
  g_gl.BindTexture(kGlTexture2d, static_cast<GLuint>(saved_texture));
}

// The context gdk_cairo_draw_from_gl last left current on the GTK thread (the window's paint
// context), and the GLX context that was then bound; see EngineMakeCurrent.
GdkGLContext *g_paint_context = nullptr;
void *g_paint_glx = nullptr;
void *(*g_glx_get_current_context)() = nullptr;

void NotePaintContext() {
  g_paint_context = gdk_gl_context_get_current();
  g_paint_glx = g_paint_context != nullptr && g_glx_get_current_context != nullptr ? g_glx_get_current_context() : nullptr;
}

// The engine makes its render context current around each draw of a frame and clears it after
// (fl_view_renderer_draw), and gdk_cairo_draw_from_gl makes the window's paint context current in
// between: three context switches a frame, each a round trip to the X server with GLX, ~1.6 ms of the
// GTK thread a frame waiting on a busy server. The render context shares its objects with the paint
// context, so the engine's draw works in either: while the paint context is still current (and
// bound: nothing switched GLX behind GDK's back), it is kept, and the clear is dropped.
void EngineMakeCurrent(GdkGLContext *context) {
  GdkGLContext *current = gdk_gl_context_get_current();
  if (current != nullptr && current == g_paint_context && current != context &&
      gdk_gl_context_get_shared_context(context) == current && g_glx_get_current_context != nullptr &&
      g_glx_get_current_context() == g_paint_glx && g_main_context_is_owner(g_main_context_default())) {
    return;
  }
  gdk_gl_context_make_current(context);
}

void EngineClearCurrent() {
  if (g_paint_context != nullptr && gdk_gl_context_get_current() == g_paint_context &&
      g_main_context_is_owner(g_main_context_default())) {
    return;
  }
  gdk_gl_context_clear_current();
}

void DrawOpaque(cairo_t *cr, GdkWindow *window, int source, int source_type, int buffer_scale, int x, int y,
                int width, int height) {
  GdkGLContext *context = gdk_gl_context_get_current();
  Held held = g_held;
  g_held = Held();
  if (source_type != kGlTexture || context == nullptr) {
    gdk_cairo_draw_from_gl(cr, window, source, source_type, buffer_scale, x, y, width, height);
    NotePaintContext();
    return;
  }
  auto *copy = static_cast<Copy *>(g_object_get_qdata(G_OBJECT(context), CopyQuark()));
  if (copy == nullptr) {
    copy = new Copy();
    g_object_set_qdata_full(G_OBJECT(context), CopyQuark(), copy, [](gpointer p) { delete static_cast<Copy *>(p); });
    g_gl.GenFramebuffers(1, &copy->framebuffer);
    g_gl.GenFramebuffers(2, copy->targets);
    g_gl.GenTextures(2, copy->textures);
  }
  int i = copy->next;
  copy->next = 1 - i;
  if (held.texture == static_cast<GLuint>(source) && held.context == context) {
    Upload(copy, i, held);
  } else {
    Blit(copy, i, static_cast<GLuint>(source));
  }
  gdk_cairo_draw_from_gl(cr, window, static_cast<int>(copy->textures[i]), kGlTexture, buffer_scale, x, y, width,
                         height);
  NotePaintContext();
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
  // The paint context kept current between frames.
  void *libgl = dlopen("libGL.so.1", RTLD_NOW | RTLD_LOCAL);
  g_glx_get_current_context =
      libgl != nullptr ? reinterpret_cast<void *(*)()>(dlsym(libgl, "glXGetCurrentContext")) : nullptr;
  if (g_glx_get_current_context != nullptr) {
    if (EngineGotPatch("gdk_gl_context_make_current", reinterpret_cast<void *>(&EngineMakeCurrent)) == 0 ||
        EngineGotPatch("gdk_gl_context_clear_current", reinterpret_cast<void *>(&EngineClearCurrent)) == 0) {
      g_warning("the engine's GL context switches not found in its relocations; frames switch as before");
    }
  }
  // Frames uploaded straight into the RGB texture; without this each is copied there on the GPU.
  // EQUO_SWT_OPAQUE_PRESENT=copy keeps the copy.
  if (off != nullptr && strcmp(off, "copy") == 0) return true;
  g_epoxy_tex_image = reinterpret_cast<TexImage2DFn *>(dlsym(RTLD_DEFAULT, "epoxy_glTexImage2D"));
  if (g_epoxy_tex_image == nullptr ||
      EngineGotPatch("epoxy_glTexImage2D", reinterpret_cast<void *>(&g_engine_tex_image)) == 0) {
    g_warning("epoxy_glTexImage2D not found in the engine's relocations; frames are copied on the GPU");
  }
  return true;
}
