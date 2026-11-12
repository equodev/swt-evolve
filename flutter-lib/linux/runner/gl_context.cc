// OpenGL for GLCanvas (dev.equo.swt.GLNative) on Linux, through GLX like SWT's GTK GLCanvas, so
// libraries that query GLX (LWJGL, JOGL) see the context they expect. The client draws the canvas,
// so each context renders into an off-screen drawable (a pbuffer, or an unmapped window where the
// server offers no pbuffer configs) and every swap reads the frame back for the client to paint.
//
// libX11 and libGL are opened at run time: a machine without them (or a Wayland-only session with
// no X display) still loads the bridge, and Create answers that there is no OpenGL.

#include "flutter_bridge.h"

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <dlfcn.h>

#include <cstring>
#include <mutex>
#include <vector>

namespace {

typedef unsigned int GLenum;
typedef int GLint;
typedef unsigned int GLuint;
typedef int GLsizei;
typedef unsigned char GLubyte;
typedef struct __GLXcontextRec* GLXContext;
typedef struct __GLXFBConfigRec* GLXFBConfig;
typedef XID GLXDrawable;
typedef XID GLXPbuffer;

constexpr GLenum kGlExtensions = 0x1F03;
constexpr GLenum kGlVersion = 0x1F02;
constexpr GLenum kGlFront = 0x0404;
constexpr GLenum kGlBack = 0x0405;
constexpr GLenum kGlReadBuffer = 0x0C02;
constexpr GLenum kGlPackRowLength = 0x0D02;
constexpr GLenum kGlPackSkipRows = 0x0D03;
constexpr GLenum kGlPackSkipPixels = 0x0D04;
constexpr GLenum kGlPackAlignment = 0x0D05;
constexpr GLenum kGlBgra = 0x80E1;
constexpr GLenum kGlUnsignedInt8888Rev = 0x8367;
constexpr GLenum kGlPixelPackBuffer = 0x88EB;
constexpr GLenum kGlPixelPackBufferBinding = 0x88ED;
constexpr GLenum kGlReadFramebuffer = 0x8CA8;
constexpr GLenum kGlReadFramebufferBinding = 0x8CAA;

constexpr int kGlxRgbaType = 0x8014;
constexpr int kGlxDoubleBuffer = 5;
constexpr int kGlxStereo = 6;
constexpr int kGlxRedSize = 8;
constexpr int kGlxGreenSize = 9;
constexpr int kGlxBlueSize = 10;
constexpr int kGlxAlphaSize = 11;
constexpr int kGlxDepthSize = 12;
constexpr int kGlxStencilSize = 13;
constexpr int kGlxAccumRedSize = 14;
constexpr int kGlxAccumGreenSize = 15;
constexpr int kGlxAccumBlueSize = 16;
constexpr int kGlxAccumAlphaSize = 17;
constexpr int kGlxSampleBuffers = 100000;
constexpr int kGlxSamples = 100001;
constexpr int kGlxDrawableType = 0x8010;
constexpr int kGlxRenderType = 0x8011;
constexpr int kGlxWindowBit = 0x1;
constexpr int kGlxPbufferBit = 0x4;
constexpr int kGlxRgbaBit = 0x1;
constexpr int kGlxPbufferWidth = 0x8041;
constexpr int kGlxPbufferHeight = 0x8040;

constexpr jlong kNoGl = -2;

struct Api {
  Display* (*XOpenDisplay)(const char*);
  int (*XDefaultScreen)(Display*);
  Window (*XRootWindow)(Display*, int);
  Colormap (*XCreateColormap)(Display*, Window, Visual*, int);
  int (*XFreeColormap)(Display*, Colormap);
  Window (*XCreateWindow)(Display*, Window, int, int, unsigned, unsigned, unsigned, int, unsigned,
                          Visual*, unsigned long, XSetWindowAttributes*);
  int (*XResizeWindow)(Display*, Window, unsigned, unsigned);
  int (*XDestroyWindow)(Display*, Window);
  int (*XFree)(void*);
  int (*XSync)(Display*, Bool);

  GLXFBConfig* (*glXChooseFBConfig)(Display*, int, const int*, int*);
  XVisualInfo* (*glXGetVisualFromFBConfig)(Display*, GLXFBConfig);
  GLXContext (*glXCreateNewContext)(Display*, GLXFBConfig, int, GLXContext, Bool);
  void (*glXDestroyContext)(Display*, GLXContext);
  GLXPbuffer (*glXCreatePbuffer)(Display*, GLXFBConfig, const int*);
  void (*glXDestroyPbuffer)(Display*, GLXPbuffer);
  Bool (*glXMakeContextCurrent)(Display*, GLXDrawable, GLXDrawable, GLXContext);
  GLXContext (*glXGetCurrentContext)();
  Display* (*glXGetCurrentDisplay)();
  GLXDrawable (*glXGetCurrentDrawable)();
  GLXDrawable (*glXGetCurrentReadDrawable)();
  void (*glXSwapBuffers)(Display*, GLXDrawable);
  void* (*glXGetProcAddressARB)(const GLubyte*);

  const GLubyte* (*glGetString)(GLenum);
  void (*glGetIntegerv)(GLenum, GLint*);
  void (*glPixelStorei)(GLenum, GLint);
  void (*glReadBuffer)(GLenum);
  void (*glReadPixels)(GLint, GLint, GLsizei, GLsizei, GLenum, GLenum, void*);
  void (*glBindBuffer)(GLenum, GLuint);
  void (*glBindFramebuffer)(GLenum, GLuint);
};

template <typename F>
bool Resolve(void* lib, const char* name, F& fn) {
  fn = reinterpret_cast<F>(dlsym(lib, name));
  return fn != nullptr;
}

std::mutex api_mutex;
Api api;
Display* display = nullptr;
bool api_tried = false;

// The X connection and the GL entry points, opened once; null when this machine has none.
Display* OpenDisplay() {
  std::lock_guard<std::mutex> lock(api_mutex);
  if (api_tried) return display;
  api_tried = true;
  void* x11 = dlopen("libX11.so.6", RTLD_NOW | RTLD_GLOBAL);
  void* gl = dlopen("libGL.so.1", RTLD_NOW | RTLD_GLOBAL);
  if (!x11 || !gl) return nullptr;
  bool ok = Resolve(x11, "XOpenDisplay", api.XOpenDisplay) &&
            Resolve(x11, "XDefaultScreen", api.XDefaultScreen) &&
            Resolve(x11, "XRootWindow", api.XRootWindow) &&
            Resolve(x11, "XCreateColormap", api.XCreateColormap) &&
            Resolve(x11, "XFreeColormap", api.XFreeColormap) &&
            Resolve(x11, "XCreateWindow", api.XCreateWindow) &&
            Resolve(x11, "XResizeWindow", api.XResizeWindow) &&
            Resolve(x11, "XDestroyWindow", api.XDestroyWindow) &&
            Resolve(x11, "XFree", api.XFree) && Resolve(x11, "XSync", api.XSync) &&
            Resolve(gl, "glXChooseFBConfig", api.glXChooseFBConfig) &&
            Resolve(gl, "glXGetVisualFromFBConfig", api.glXGetVisualFromFBConfig) &&
            Resolve(gl, "glXCreateNewContext", api.glXCreateNewContext) &&
            Resolve(gl, "glXDestroyContext", api.glXDestroyContext) &&
            Resolve(gl, "glXCreatePbuffer", api.glXCreatePbuffer) &&
            Resolve(gl, "glXDestroyPbuffer", api.glXDestroyPbuffer) &&
            Resolve(gl, "glXMakeContextCurrent", api.glXMakeContextCurrent) &&
            Resolve(gl, "glXGetCurrentContext", api.glXGetCurrentContext) &&
            Resolve(gl, "glXGetCurrentDisplay", api.glXGetCurrentDisplay) &&
            Resolve(gl, "glXGetCurrentDrawable", api.glXGetCurrentDrawable) &&
            Resolve(gl, "glXGetCurrentReadDrawable", api.glXGetCurrentReadDrawable) &&
            Resolve(gl, "glXSwapBuffers", api.glXSwapBuffers) &&
            Resolve(gl, "glXGetProcAddressARB", api.glXGetProcAddressARB) &&
            Resolve(gl, "glGetString", api.glGetString) &&
            Resolve(gl, "glGetIntegerv", api.glGetIntegerv) &&
            Resolve(gl, "glPixelStorei", api.glPixelStorei) &&
            Resolve(gl, "glReadBuffer", api.glReadBuffer) &&
            Resolve(gl, "glReadPixels", api.glReadPixels);
  if (!ok) return nullptr;
  api.glBindBuffer = reinterpret_cast<void (*)(GLenum, GLuint)>(
      api.glXGetProcAddressARB(reinterpret_cast<const GLubyte*>("glBindBuffer")));
  api.glBindFramebuffer = reinterpret_cast<void (*)(GLenum, GLuint)>(
      api.glXGetProcAddressARB(reinterpret_cast<const GLubyte*>("glBindFramebufferEXT")));
  // GDK has its own connection; this one only carries the off-screen drawables.
  display = api.XOpenDisplay(nullptr);
  return display;
}

bool Loaded() {
  std::lock_guard<std::mutex> lock(api_mutex);
  return display != nullptr;
}

struct Surface {
  GLXContext context = nullptr;
  GLXFBConfig config = nullptr;
  GLXDrawable drawable = 0;
  bool pbuffer = false;
  Colormap colormap = 0;
  bool double_buffer = false;
  // Probed at the first swap, with the context current: whether reading may touch FBO / PBO state.
  bool probed = false;
  bool has_fbo = false;
  bool has_pbo = false;
};

Surface* FromHandle(jlong handle) {
  return reinterpret_cast<Surface*>(static_cast<intptr_t>(handle));
}

// The GLData fields in dev.equo.swt.GLNative's order, mapped as SWT's GTK GLCanvas maps them.
std::vector<int> FbConfigAttributes(const jint* d, int drawable_type) {
  std::vector<int> a = {kGlxDrawableType, drawable_type, kGlxRenderType, kGlxRgbaBit,
                        kGlxDoubleBuffer, d[0] ? True : False};
  if (d[1]) a.insert(a.end(), {kGlxStereo, True});
  if (d[2] > 0) a.insert(a.end(), {kGlxRedSize, d[2]});
  if (d[3] > 0) a.insert(a.end(), {kGlxGreenSize, d[3]});
  if (d[4] > 0) a.insert(a.end(), {kGlxBlueSize, d[4]});
  if (d[5] > 0) a.insert(a.end(), {kGlxAlphaSize, d[5]});
  if (d[6] > 0) a.insert(a.end(), {kGlxDepthSize, d[6]});
  if (d[7] > 0) a.insert(a.end(), {kGlxStencilSize, d[7]});
  if (d[8] > 0) a.insert(a.end(), {kGlxAccumRedSize, d[8]});
  if (d[9] > 0) a.insert(a.end(), {kGlxAccumGreenSize, d[9]});
  if (d[10] > 0) a.insert(a.end(), {kGlxAccumBlueSize, d[10]});
  if (d[11] > 0) a.insert(a.end(), {kGlxAccumAlphaSize, d[11]});
  if (d[12] > 0) a.insert(a.end(), {kGlxSampleBuffers, d[12]});
  if (d[13] > 0) a.insert(a.end(), {kGlxSamples, d[13]});
  a.push_back(None);
  return a;
}

GLXFBConfig ChooseConfig(const jint* d, int drawable_type) {
  std::vector<int> attributes = FbConfigAttributes(d, drawable_type);
  int count = 0;
  GLXFBConfig* configs =
      api.glXChooseFBConfig(display, api.XDefaultScreen(display), attributes.data(), &count);
  if (!configs) return nullptr;
  GLXFBConfig config = count > 0 ? configs[0] : nullptr;
  api.XFree(configs);
  return config;
}

// A drawable of |width| x |height| for |surface|'s config, or 0.
GLXDrawable NewDrawable(Surface* surface, int width, int height) {
  if (surface->pbuffer) {
    const int attributes[] = {kGlxPbufferWidth, width, kGlxPbufferHeight, height, None};
    return api.glXCreatePbuffer(display, surface->config, attributes);
  }
  XVisualInfo* visual = api.glXGetVisualFromFBConfig(display, surface->config);
  if (!visual) return 0;
  Window root = api.XRootWindow(display, visual->screen);
  if (!surface->colormap) surface->colormap = api.XCreateColormap(display, root, visual->visual, AllocNone);
  XSetWindowAttributes attributes;
  std::memset(&attributes, 0, sizeof(attributes));
  attributes.colormap = surface->colormap;
  // Never mapped: the window only gives the context a default framebuffer.
  Window window = api.XCreateWindow(display, root, 0, 0, width, height, 0, visual->depth, InputOutput,
                                    visual->visual, CWColormap | CWBorderPixel, &attributes);
  api.XFree(visual);
  return window;
}

void FreeDrawable(Surface* surface, GLXDrawable drawable) {
  if (!drawable) return;
  if (surface->pbuffer) {
    api.glXDestroyPbuffer(display, drawable);
  } else {
    api.XDestroyWindow(display, drawable);
  }
}

bool HasExtension(const char* extensions, const char* name) {
  if (!extensions) return false;
  size_t length = std::strlen(name);
  for (const char* p = std::strstr(extensions, name); p; p = std::strstr(p + length, name)) {
    if ((p == extensions || p[-1] == ' ') && (p[length] == ' ' || p[length] == '\0')) return true;
  }
  return false;
}

void Probe(Surface* surface) {
  if (surface->probed) return;
  surface->probed = true;
  const char* version = reinterpret_cast<const char*>(api.glGetString(kGlVersion));
  const char* extensions = reinterpret_cast<const char*>(api.glGetString(kGlExtensions));
  int major = version ? version[0] - '0' : 1;
  int minor = version && version[1] == '.' ? version[2] - '0' : 0;
  surface->has_fbo = api.glBindFramebuffer &&
                     (major >= 3 || HasExtension(extensions, "GL_EXT_framebuffer_object") ||
                      HasExtension(extensions, "GL_ARB_framebuffer_object"));
  surface->has_pbo = api.glBindBuffer && (major > 2 || (major == 2 && minor >= 1) ||
                                          HasExtension(extensions, "GL_ARB_pixel_buffer_object"));
}

}  // namespace

JNIEXPORT jlong JNICALL Java_dev_equo_swt_GLNative_Create(JNIEnv* env, jclass cls, jintArray glData,
                                                          jlong share, jint width, jint height) {
  if (!OpenDisplay()) return kNoGl;
  jint d[14] = {};
  jsize count = env->GetArrayLength(glData);
  env->GetIntArrayRegion(glData, 0, count < 14 ? count : 14, d);
  int w = width > 0 ? width : 1, h = height > 0 ? height : 1;
  auto* surface = new Surface();
  surface->double_buffer = d[0] != 0;
  std::lock_guard<std::mutex> lock(api_mutex);
  surface->config = ChooseConfig(d, kGlxPbufferBit);
  surface->pbuffer = surface->config != nullptr;
  if (!surface->config) surface->config = ChooseConfig(d, kGlxWindowBit);
  if (surface->config) {
    Surface* shared = FromHandle(share);
    surface->context = api.glXCreateNewContext(display, surface->config, kGlxRgbaType,
                                               shared ? shared->context : nullptr, True);
  }
  if (surface->context) surface->drawable = NewDrawable(surface, w, h);
  if (!surface->drawable) {
    if (surface->context) api.glXDestroyContext(display, surface->context);
    if (surface->colormap) api.XFreeColormap(display, surface->colormap);
    delete surface;
    return 0;
  }
  api.XSync(display, False);
  return static_cast<jlong>(reinterpret_cast<intptr_t>(surface));
}

JNIEXPORT jboolean JNICALL Java_dev_equo_swt_GLNative_SetSize(JNIEnv* env, jclass cls, jlong handle,
                                                              jint width, jint height) {
  Surface* surface = FromHandle(handle);
  if (!surface || !Loaded()) return JNI_FALSE;
  int w = width > 0 ? width : 1, h = height > 0 ? height : 1;
  std::lock_guard<std::mutex> lock(api_mutex);
  if (!surface->pbuffer) {
    api.XResizeWindow(display, surface->drawable, w, h);
    api.XSync(display, False);
    return JNI_TRUE;
  }
  GLXDrawable resized = NewDrawable(surface, w, h);
  if (!resized) return JNI_FALSE;
  if (api.glXGetCurrentContext() == surface->context) {
    api.glXMakeContextCurrent(display, resized, resized, surface->context);
  }
  FreeDrawable(surface, surface->drawable);
  surface->drawable = resized;
  return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_dev_equo_swt_GLNative_MakeCurrent(JNIEnv* env, jclass cls,
                                                              jlong handle) {
  if (!Loaded()) return;
  Surface* surface = FromHandle(handle);
  std::lock_guard<std::mutex> lock(api_mutex);
  if (surface) {
    api.glXMakeContextCurrent(display, surface->drawable, surface->drawable, surface->context);
  } else if (api.glXGetCurrentContext()) {
    api.glXMakeContextCurrent(api.glXGetCurrentDisplay(), None, None, nullptr);
  }
}

JNIEXPORT jboolean JNICALL Java_dev_equo_swt_GLNative_IsCurrent(JNIEnv* env, jclass cls,
                                                                jlong handle) {
  Surface* surface = FromHandle(handle);
  if (!surface || !Loaded()) return JNI_FALSE;
  return api.glXGetCurrentContext() == surface->context ? JNI_TRUE : JNI_FALSE;
}

// Reads the frame about to be swapped into |pixels| (width * height 0xAARRGGBB, top row first),
// then swaps. The application's read framebuffer, read buffer and pixel-pack state are left as
// they were.
JNIEXPORT jboolean JNICALL Java_dev_equo_swt_GLNative_Swap(JNIEnv* env, jclass cls, jlong handle,
                                                           jintArray pixels, jint width,
                                                           jint height) {
  Surface* surface = FromHandle(handle);
  if (!surface || !Loaded()) return JNI_FALSE;
  std::lock_guard<std::mutex> lock(api_mutex);
  bool read = false;
  if (pixels && width > 0 && height > 0 && env->GetArrayLength(pixels) >= width * height) {
    GLXContext previous = api.glXGetCurrentContext();
    Display* previous_display = api.glXGetCurrentDisplay();
    GLXDrawable previous_draw = api.glXGetCurrentDrawable();
    GLXDrawable previous_read = api.glXGetCurrentReadDrawable();
    if (previous != surface->context) {
      api.glXMakeContextCurrent(display, surface->drawable, surface->drawable, surface->context);
    }
    Probe(surface);
    GLint read_framebuffer = 0, read_buffer = 0, pack_buffer = 0, alignment = 0;
    GLint row_length = 0, skip_rows = 0, skip_pixels = 0;
    if (surface->has_fbo) {
      api.glGetIntegerv(kGlReadFramebufferBinding, &read_framebuffer);
      api.glBindFramebuffer(kGlReadFramebuffer, 0);
    }
    api.glGetIntegerv(kGlReadBuffer, &read_buffer);
    if (surface->has_pbo) {
      api.glGetIntegerv(kGlPixelPackBufferBinding, &pack_buffer);
      api.glBindBuffer(kGlPixelPackBuffer, 0);
    }
    api.glGetIntegerv(kGlPackAlignment, &alignment);
    api.glGetIntegerv(kGlPackRowLength, &row_length);
    api.glGetIntegerv(kGlPackSkipRows, &skip_rows);
    api.glGetIntegerv(kGlPackSkipPixels, &skip_pixels);
    api.glPixelStorei(kGlPackAlignment, 4);
    api.glPixelStorei(kGlPackRowLength, 0);
    api.glPixelStorei(kGlPackSkipRows, 0);
    api.glPixelStorei(kGlPackSkipPixels, 0);
    api.glReadBuffer(surface->double_buffer ? kGlBack : kGlFront);
    std::vector<jint> frame(static_cast<size_t>(width) * height);
    api.glReadPixels(0, 0, width, height, kGlBgra, kGlUnsignedInt8888Rev, frame.data());
    api.glReadBuffer(read_buffer);
    api.glPixelStorei(kGlPackAlignment, alignment);
    api.glPixelStorei(kGlPackRowLength, row_length);
    api.glPixelStorei(kGlPackSkipRows, skip_rows);
    api.glPixelStorei(kGlPackSkipPixels, skip_pixels);
    if (surface->has_pbo) api.glBindBuffer(kGlPixelPackBuffer, pack_buffer);
    if (surface->has_fbo) api.glBindFramebuffer(kGlReadFramebuffer, read_framebuffer);
    // GL rows run bottom-up.
    for (jint row = 0; row < height; row++) {
      env->SetIntArrayRegion(pixels, row * width, width,
                             frame.data() + static_cast<size_t>(height - 1 - row) * width);
    }
    if (previous != surface->context) {
      if (previous) {
        api.glXMakeContextCurrent(previous_display, previous_draw, previous_read, previous);
      } else {
        api.glXMakeContextCurrent(display, None, None, nullptr);
      }
    }
    read = true;
  }
  if (surface->double_buffer) api.glXSwapBuffers(display, surface->drawable);
  return read ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_dev_equo_swt_GLNative_Destroy(JNIEnv* env, jclass cls, jlong handle) {
  Surface* surface = FromHandle(handle);
  if (!surface || !Loaded()) return;
  std::lock_guard<std::mutex> lock(api_mutex);
  if (api.glXGetCurrentContext() == surface->context) {
    api.glXMakeContextCurrent(display, None, None, nullptr);
  }
  api.glXDestroyContext(display, surface->context);
  FreeDrawable(surface, surface->drawable);
  if (surface->colormap) api.XFreeColormap(display, surface->colormap);
  api.XSync(display, False);
  delete surface;
}
