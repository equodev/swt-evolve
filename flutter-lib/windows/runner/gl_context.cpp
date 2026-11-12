// OpenGL for GLCanvas (dev.equo.swt.GLNative) on Windows, through WGL like SWT's win32 GLCanvas, so
// libraries that query WGL (LWJGL, JOGL) see the context they expect. The client draws the canvas,
// so each context renders into an off-screen drawable (a pbuffer where WGL_ARB_pbuffer is offered,
// else a window that is never shown) and every swap reads the frame back for the client to paint.
//
// opengl32 is opened at run time, so a machine without it still loads the bridge and Create answers
// that there is no OpenGL.

#include "flutter_bridge.h"

#include <windows.h>

#include <cstdint>
#include <cstring>
#include <mutex>
#include <vector>

namespace {

typedef unsigned int GLenum;
typedef int GLint;
typedef unsigned int GLuint;
typedef int GLsizei;
typedef unsigned char GLubyte;
DECLARE_HANDLE(HPBUFFERARB);

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
constexpr GLenum kGlUnsignedByte = 0x1401;
constexpr GLenum kGlPixelPackBuffer = 0x88EB;
constexpr GLenum kGlPixelPackBufferBinding = 0x88ED;
constexpr GLenum kGlReadFramebuffer = 0x8CA8;
constexpr GLenum kGlReadFramebufferBinding = 0x8CAA;

constexpr int kWglDrawToPbuffer = 0x202D;
constexpr int kWglSupportOpenGl = 0x2010;
constexpr int kWglDoubleBuffer = 0x2011;
constexpr int kWglStereo = 0x2012;
constexpr int kWglPixelType = 0x2013;
constexpr int kWglTypeRgba = 0x202B;
constexpr int kWglAcceleration = 0x2003;
constexpr int kWglFullAcceleration = 0x2027;
constexpr int kWglRedBits = 0x2015;
constexpr int kWglGreenBits = 0x2017;
constexpr int kWglBlueBits = 0x2019;
constexpr int kWglAlphaBits = 0x201B;
constexpr int kWglAccumRedBits = 0x201E;
constexpr int kWglAccumGreenBits = 0x201F;
constexpr int kWglAccumBlueBits = 0x2020;
constexpr int kWglAccumAlphaBits = 0x2021;
constexpr int kWglDepthBits = 0x2022;
constexpr int kWglStencilBits = 0x2023;
constexpr int kWglSampleBuffers = 0x2041;
constexpr int kWglSamples = 0x2042;

constexpr jlong kNoGl = -2;
constexpr wchar_t kWindowClass[] = L"EquoGLContext";

struct Api {
  HGLRC(WINAPI* wglCreateContext)(HDC);
  BOOL(WINAPI* wglDeleteContext)(HGLRC);
  BOOL(WINAPI* wglMakeCurrent)(HDC, HGLRC);
  HGLRC(WINAPI* wglGetCurrentContext)();
  HDC(WINAPI* wglGetCurrentDC)();
  BOOL(WINAPI* wglShareLists)(HGLRC, HGLRC);
  PROC(WINAPI* wglGetProcAddress)(LPCSTR);

  const GLubyte*(WINAPI* glGetString)(GLenum);
  void(WINAPI* glGetIntegerv)(GLenum, GLint*);
  void(WINAPI* glPixelStorei)(GLenum, GLint);
  void(WINAPI* glReadBuffer)(GLenum);
  void(WINAPI* glReadPixels)(GLint, GLint, GLsizei, GLsizei, GLenum, GLenum, void*);

  // WGL_ARB_pixel_format + WGL_ARB_pbuffer; null where the driver offers neither.
  BOOL(WINAPI* wglChoosePixelFormatARB)(HDC, const int*, const FLOAT*, UINT, int*, UINT*);
  HPBUFFERARB(WINAPI* wglCreatePbufferARB)(HDC, int, int, int, const int*);
  HDC(WINAPI* wglGetPbufferDCARB)(HPBUFFERARB);
  int(WINAPI* wglReleasePbufferDCARB)(HPBUFFERARB, HDC);
  BOOL(WINAPI* wglDestroyPbufferARB)(HPBUFFERARB);
  bool multisample;
};

template <typename F>
bool Resolve(HMODULE lib, const char* name, F& fn) {
  fn = reinterpret_cast<F>(::GetProcAddress(lib, name));
  return fn != nullptr;
}

template <typename F>
void ResolveWgl(const char* name, F& fn, Api& api) {
  PROC proc = api.wglGetProcAddress(name);
  // Some drivers answer small integers instead of null for names they do not know.
  intptr_t value = reinterpret_cast<intptr_t>(proc);
  fn = value == 0 || value == 1 || value == 2 || value == 3 || value == -1 ? nullptr
                                                                           : reinterpret_cast<F>(proc);
}

bool HasExtension(const char* extensions, const char* name) {
  if (!extensions) return false;
  size_t length = std::strlen(name);
  for (const char* p = std::strstr(extensions, name); p; p = std::strstr(p + length, name)) {
    if ((p == extensions || p[-1] == ' ') && (p[length] == ' ' || p[length] == '\0')) return true;
  }
  return false;
}

std::once_flag api_once;
Api api;
bool loaded = false;
// Hidden window that loads the WGL extensions and is the device pbuffers are created against.
HWND device_window = nullptr;
HDC device_dc = nullptr;

HWND NewWindow(int width, int height) {
  return ::CreateWindowExW(WS_EX_TOOLWINDOW, kWindowClass, L"", WS_POPUP | WS_CLIPSIBLINGS | WS_CLIPCHILDREN,
                           0, 0, width, height, nullptr, nullptr, ::GetModuleHandleW(nullptr), nullptr);
}

// The GLData fields in dev.equo.swt.GLNative's order, mapped as SWT's win32 GLCanvas maps them.
PIXELFORMATDESCRIPTOR Descriptor(const jint* d) {
  PIXELFORMATDESCRIPTOR pfd;
  std::memset(&pfd, 0, sizeof(pfd));
  pfd.nSize = sizeof(pfd);
  pfd.nVersion = 1;
  pfd.dwFlags = PFD_DRAW_TO_WINDOW | PFD_SUPPORT_OPENGL;
  if (d[0]) pfd.dwFlags |= PFD_DOUBLEBUFFER;
  if (d[1]) pfd.dwFlags |= PFD_STEREO;
  pfd.iPixelType = PFD_TYPE_RGBA;
  pfd.cColorBits = static_cast<BYTE>(d[2] + d[3] + d[4]);
  pfd.cRedBits = static_cast<BYTE>(d[2]);
  pfd.cGreenBits = static_cast<BYTE>(d[3]);
  pfd.cBlueBits = static_cast<BYTE>(d[4]);
  pfd.cAlphaBits = static_cast<BYTE>(d[5]);
  pfd.cDepthBits = static_cast<BYTE>(d[6]);
  pfd.cStencilBits = static_cast<BYTE>(d[7]);
  pfd.cAccumRedBits = static_cast<BYTE>(d[8]);
  pfd.cAccumGreenBits = static_cast<BYTE>(d[9]);
  pfd.cAccumBlueBits = static_cast<BYTE>(d[10]);
  pfd.cAccumAlphaBits = static_cast<BYTE>(d[11]);
  pfd.cAccumBits = static_cast<BYTE>(d[8] + d[9] + d[10] + d[11]);
  pfd.iLayerType = PFD_MAIN_PLANE;
  return pfd;
}

void Load() {
  HMODULE gl = ::LoadLibraryW(L"opengl32.dll");
  if (!gl) return;
  bool ok = Resolve(gl, "wglCreateContext", api.wglCreateContext) &&
            Resolve(gl, "wglDeleteContext", api.wglDeleteContext) &&
            Resolve(gl, "wglMakeCurrent", api.wglMakeCurrent) &&
            Resolve(gl, "wglGetCurrentContext", api.wglGetCurrentContext) &&
            Resolve(gl, "wglGetCurrentDC", api.wglGetCurrentDC) &&
            Resolve(gl, "wglShareLists", api.wglShareLists) &&
            Resolve(gl, "wglGetProcAddress", api.wglGetProcAddress) &&
            Resolve(gl, "glGetString", api.glGetString) &&
            Resolve(gl, "glGetIntegerv", api.glGetIntegerv) &&
            Resolve(gl, "glPixelStorei", api.glPixelStorei) &&
            Resolve(gl, "glReadBuffer", api.glReadBuffer) &&
            Resolve(gl, "glReadPixels", api.glReadPixels);
  if (!ok) return;
  WNDCLASSEXW window_class;
  std::memset(&window_class, 0, sizeof(window_class));
  window_class.cbSize = sizeof(window_class);
  window_class.style = CS_OWNDC;
  window_class.lpfnWndProc = ::DefWindowProcW;
  window_class.hInstance = ::GetModuleHandleW(nullptr);
  window_class.lpszClassName = kWindowClass;
  if (!::RegisterClassExW(&window_class) && ::GetLastError() != ERROR_CLASS_ALREADY_EXISTS) return;
  device_window = NewWindow(1, 1);
  if (!device_window) return;
  device_dc = ::GetDC(device_window);
  const jint defaults[14] = {1, 0, 8, 8, 8, 8, 24, 8};
  PIXELFORMATDESCRIPTOR pfd = Descriptor(defaults);
  int format = ::ChoosePixelFormat(device_dc, &pfd);
  if (!format || !::SetPixelFormat(device_dc, format, &pfd)) return;
  loaded = true;
  // The ARB entry points are only offered while a context is current.
  HGLRC probe = api.wglCreateContext(device_dc);
  if (!probe) return;
  HGLRC previous = api.wglGetCurrentContext();
  HDC previous_dc = api.wglGetCurrentDC();
  if (api.wglMakeCurrent(device_dc, probe)) {
    const char*(WINAPI * extensions_string)(HDC) = nullptr;
    ResolveWgl("wglGetExtensionsStringARB", extensions_string, api);
    const char* extensions = extensions_string ? extensions_string(device_dc) : nullptr;
    if (HasExtension(extensions, "WGL_ARB_pixel_format") && HasExtension(extensions, "WGL_ARB_pbuffer")) {
      ResolveWgl("wglChoosePixelFormatARB", api.wglChoosePixelFormatARB, api);
      ResolveWgl("wglCreatePbufferARB", api.wglCreatePbufferARB, api);
      ResolveWgl("wglGetPbufferDCARB", api.wglGetPbufferDCARB, api);
      ResolveWgl("wglReleasePbufferDCARB", api.wglReleasePbufferDCARB, api);
      ResolveWgl("wglDestroyPbufferARB", api.wglDestroyPbufferARB, api);
      if (!api.wglChoosePixelFormatARB || !api.wglCreatePbufferARB || !api.wglGetPbufferDCARB ||
          !api.wglReleasePbufferDCARB || !api.wglDestroyPbufferARB) {
        api.wglCreatePbufferARB = nullptr;
      }
      api.multisample = HasExtension(extensions, "WGL_ARB_multisample");
    }
  }
  api.wglMakeCurrent(previous_dc, previous);
  api.wglDeleteContext(probe);
}

bool Loaded() {
  std::call_once(api_once, Load);
  return loaded;
}

struct Surface {
  HGLRC context = nullptr;
  HDC dc = nullptr;
  HWND window = nullptr;
  DWORD window_thread = 0;
  HPBUFFERARB pbuffer = nullptr;
  int format = 0;
  bool double_buffer = false;
  // Probed at the first swap, with the context current: whether reading may touch FBO / PBO state.
  bool probed = false;
  bool has_fbo = false;
  bool has_pbo = false;
  void(WINAPI* glBindBuffer)(GLenum, GLuint) = nullptr;
  void(WINAPI* glBindFramebuffer)(GLenum, GLuint) = nullptr;
};

Surface* FromHandle(jlong handle) {
  return reinterpret_cast<Surface*>(static_cast<intptr_t>(handle));
}

int ChoosePbufferFormat(const jint* d) {
  std::vector<int> a = {kWglDrawToPbuffer, TRUE, kWglSupportOpenGl, TRUE, kWglAcceleration, kWglFullAcceleration,
                        kWglPixelType, kWglTypeRgba, kWglDoubleBuffer, d[0] ? TRUE : FALSE};
  if (d[1]) a.insert(a.end(), {kWglStereo, TRUE});
  if (d[2] > 0) a.insert(a.end(), {kWglRedBits, d[2]});
  if (d[3] > 0) a.insert(a.end(), {kWglGreenBits, d[3]});
  if (d[4] > 0) a.insert(a.end(), {kWglBlueBits, d[4]});
  if (d[5] > 0) a.insert(a.end(), {kWglAlphaBits, d[5]});
  if (d[6] > 0) a.insert(a.end(), {kWglDepthBits, d[6]});
  if (d[7] > 0) a.insert(a.end(), {kWglStencilBits, d[7]});
  if (d[8] > 0) a.insert(a.end(), {kWglAccumRedBits, d[8]});
  if (d[9] > 0) a.insert(a.end(), {kWglAccumGreenBits, d[9]});
  if (d[10] > 0) a.insert(a.end(), {kWglAccumBlueBits, d[10]});
  if (d[11] > 0) a.insert(a.end(), {kWglAccumAlphaBits, d[11]});
  if (api.multisample && d[12] > 0) a.insert(a.end(), {kWglSampleBuffers, d[12]});
  if (api.multisample && d[13] > 0) a.insert(a.end(), {kWglSamples, d[13]});
  a.push_back(0);
  int format = 0;
  UINT count = 0;
  if (!api.wglChoosePixelFormatARB(device_dc, a.data(), nullptr, 1, &format, &count) || count == 0) return 0;
  return format;
}

bool NewPbuffer(Surface* surface, int width, int height, HPBUFFERARB* pbuffer, HDC* dc) {
  const int attributes[] = {0};
  *pbuffer = api.wglCreatePbufferARB(device_dc, surface->format, width, height, attributes);
  if (!*pbuffer) return false;
  *dc = api.wglGetPbufferDCARB(*pbuffer);
  if (*dc) return true;
  api.wglDestroyPbufferARB(*pbuffer);
  *pbuffer = nullptr;
  return false;
}

void FreePbuffer(HPBUFFERARB pbuffer, HDC dc) {
  api.wglReleasePbufferDCARB(pbuffer, dc);
  api.wglDestroyPbufferARB(pbuffer);
}

bool NewWindowDrawable(Surface* surface, const jint* d, int width, int height) {
  surface->window = NewWindow(width, height);
  if (!surface->window) return false;
  surface->window_thread = ::GetCurrentThreadId();
  surface->dc = ::GetDC(surface->window);
  PIXELFORMATDESCRIPTOR pfd = Descriptor(d);
  int format = ::ChoosePixelFormat(surface->dc, &pfd);
  return format && ::SetPixelFormat(surface->dc, format, &pfd);
}

void FreeDrawable(Surface* surface) {
  if (surface->pbuffer) {
    FreePbuffer(surface->pbuffer, surface->dc);
  } else if (surface->window) {
    // A window is destroyed by the thread that created it; CS_OWNDC releases its DC with it.
    if (::GetCurrentThreadId() == surface->window_thread) {
      ::DestroyWindow(surface->window);
    } else {
      ::PostMessageW(surface->window, WM_CLOSE, 0, 0);
    }
  }
  surface->pbuffer = nullptr;
  surface->window = nullptr;
  surface->dc = nullptr;
}

template <typename F>
void ResolveGl(const char* name, F& fn) {
  ResolveWgl(name, fn, api);
}

void Probe(Surface* surface) {
  if (surface->probed) return;
  surface->probed = true;
  const char* version = reinterpret_cast<const char*>(api.glGetString(kGlVersion));
  const char* extensions = reinterpret_cast<const char*>(api.glGetString(kGlExtensions));
  int major = version ? version[0] - '0' : 1;
  int minor = version && version[1] == '.' ? version[2] - '0' : 0;
  ResolveGl("glBindBuffer", surface->glBindBuffer);
  ResolveGl("glBindFramebufferEXT", surface->glBindFramebuffer);
  surface->has_fbo = surface->glBindFramebuffer &&
                     (major >= 3 || HasExtension(extensions, "GL_EXT_framebuffer_object") ||
                      HasExtension(extensions, "GL_ARB_framebuffer_object"));
  surface->has_pbo = surface->glBindBuffer && (major > 2 || (major == 2 && minor >= 1) ||
                                               HasExtension(extensions, "GL_ARB_pixel_buffer_object"));
}

}  // namespace

JNIEXPORT jlong JNICALL Java_dev_equo_swt_GLNative_Create(JNIEnv* env, jclass cls, jintArray glData,
                                                          jlong share, jint width, jint height) {
  if (!Loaded()) return kNoGl;
  jint d[14] = {};
  jsize count = env->GetArrayLength(glData);
  env->GetIntArrayRegion(glData, 0, count < 14 ? count : 14, d);
  int w = width > 0 ? width : 1, h = height > 0 ? height : 1;
  auto* surface = new Surface();
  surface->double_buffer = d[0] != 0;
  if (api.wglCreatePbufferARB) {
    surface->format = ChoosePbufferFormat(d);
    if (surface->format) NewPbuffer(surface, w, h, &surface->pbuffer, &surface->dc);
  }
  bool drawable = surface->pbuffer != nullptr || NewWindowDrawable(surface, d, w, h);
  if (drawable) surface->context = api.wglCreateContext(surface->dc);
  Surface* shared = FromHandle(share);
  if (surface->context && shared && !api.wglShareLists(shared->context, surface->context)) {
    api.wglDeleteContext(surface->context);
    surface->context = nullptr;
  }
  if (!surface->context) {
    FreeDrawable(surface);
    delete surface;
    return 0;
  }
  return static_cast<jlong>(reinterpret_cast<intptr_t>(surface));
}

JNIEXPORT jboolean JNICALL Java_dev_equo_swt_GLNative_SetSize(JNIEnv* env, jclass cls, jlong handle,
                                                              jint width, jint height) {
  Surface* surface = FromHandle(handle);
  if (!surface || !Loaded()) return JNI_FALSE;
  int w = width > 0 ? width : 1, h = height > 0 ? height : 1;
  if (!surface->pbuffer) {
    return ::SetWindowPos(surface->window, nullptr, 0, 0, w, h, SWP_NOMOVE | SWP_NOZORDER | SWP_NOACTIVATE)
               ? JNI_TRUE
               : JNI_FALSE;
  }
  HPBUFFERARB pbuffer = nullptr;
  HDC dc = nullptr;
  if (!NewPbuffer(surface, w, h, &pbuffer, &dc)) return JNI_FALSE;
  if (api.wglGetCurrentContext() == surface->context) api.wglMakeCurrent(dc, surface->context);
  FreePbuffer(surface->pbuffer, surface->dc);
  surface->pbuffer = pbuffer;
  surface->dc = dc;
  return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_dev_equo_swt_GLNative_MakeCurrent(JNIEnv* env, jclass cls,
                                                              jlong handle) {
  if (!Loaded()) return;
  Surface* surface = FromHandle(handle);
  if (surface) {
    api.wglMakeCurrent(surface->dc, surface->context);
  } else if (api.wglGetCurrentContext()) {
    api.wglMakeCurrent(nullptr, nullptr);
  }
}

JNIEXPORT jboolean JNICALL Java_dev_equo_swt_GLNative_IsCurrent(JNIEnv* env, jclass cls,
                                                                jlong handle) {
  Surface* surface = FromHandle(handle);
  if (!surface || !Loaded()) return JNI_FALSE;
  return api.wglGetCurrentContext() == surface->context ? JNI_TRUE : JNI_FALSE;
}

// Reads the frame about to be swapped into |pixels| (width * height 0xAARRGGBB, top row first),
// then swaps. The application's current context, read framebuffer, read buffer and pixel-pack
// state are left as they were.
JNIEXPORT jboolean JNICALL Java_dev_equo_swt_GLNative_Swap(JNIEnv* env, jclass cls, jlong handle,
                                                           jintArray pixels, jint width,
                                                           jint height) {
  Surface* surface = FromHandle(handle);
  if (!surface || !Loaded()) return JNI_FALSE;
  bool read = false;
  if (pixels && width > 0 && height > 0 && env->GetArrayLength(pixels) >= width * height) {
    HGLRC previous = api.wglGetCurrentContext();
    HDC previous_dc = api.wglGetCurrentDC();
    if (previous != surface->context) api.wglMakeCurrent(surface->dc, surface->context);
    Probe(surface);
    GLint read_framebuffer = 0, read_buffer = 0, pack_buffer = 0, alignment = 0;
    GLint row_length = 0, skip_rows = 0, skip_pixels = 0;
    if (surface->has_fbo) {
      api.glGetIntegerv(kGlReadFramebufferBinding, &read_framebuffer);
      surface->glBindFramebuffer(kGlReadFramebuffer, 0);
    }
    api.glGetIntegerv(kGlReadBuffer, &read_buffer);
    if (surface->has_pbo) {
      api.glGetIntegerv(kGlPixelPackBufferBinding, &pack_buffer);
      surface->glBindBuffer(kGlPixelPackBuffer, 0);
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
    // BGRA bytes are 0xAARRGGBB ints on little-endian Windows. GL_UNSIGNED_INT_8_8_8_8_REV gives the
    // same but is GL 1.2, and opengl32's own GDI renderer is GL 1.1 with EXT_bgra.
    api.glReadPixels(0, 0, width, height, kGlBgra, kGlUnsignedByte, frame.data());
    api.glReadBuffer(read_buffer);
    api.glPixelStorei(kGlPackAlignment, alignment);
    api.glPixelStorei(kGlPackRowLength, row_length);
    api.glPixelStorei(kGlPackSkipRows, skip_rows);
    api.glPixelStorei(kGlPackSkipPixels, skip_pixels);
    if (surface->has_pbo) surface->glBindBuffer(kGlPixelPackBuffer, pack_buffer);
    if (surface->has_fbo) surface->glBindFramebuffer(kGlReadFramebuffer, read_framebuffer);
    // GL rows run bottom-up.
    for (jint row = 0; row < height; row++) {
      env->SetIntArrayRegion(pixels, row * width, width,
                             frame.data() + static_cast<size_t>(height - 1 - row) * width);
    }
    if (previous != surface->context) api.wglMakeCurrent(previous_dc, previous);
    read = true;
  }
  if (surface->double_buffer) ::SwapBuffers(surface->dc);
  return read ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL Java_dev_equo_swt_GLNative_Destroy(JNIEnv* env, jclass cls, jlong handle) {
  Surface* surface = FromHandle(handle);
  if (!surface || !Loaded()) return;
  if (api.wglGetCurrentContext() == surface->context) api.wglMakeCurrent(nullptr, nullptr);
  api.wglDeleteContext(surface->context);
  FreeDrawable(surface);
  delete surface;
}
