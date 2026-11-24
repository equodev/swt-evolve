// The frames an OpenGL canvas embedded through SWT_AWT presents (dev.equo.swt.awt.GlPresentHook).
//
// Such a canvas renders into its own X window, a child of the never-mapped window of the
// JLightweightFrame that hosts the Swing tree, so nothing it draws is ever on screen or readable
// afterwards. The application ends every frame with glXSwapBuffers, drawn and not yet swapped, its
// context current: the import of glXSwapBuffers in every loaded object is rebound to a function
// that reads the back buffer of a tracked window before the real swap runs.
//
// libGL and libX11 are opened at run time, as gl_context.cc does, so the bridge still loads where
// they are missing. Calls that reach glXSwapBuffers through glXGetProcAddress or dlsym are not
// rebound.

#include "flutter_bridge.h"

#include <X11/Xlib.h>
#include <dlfcn.h>
#include <elf.h>
#include <jawt.h>
#include <jawt_md.h>
#include <link.h>
#include <sys/mman.h>
#include <unistd.h>

#include <cstdint>
#include <cstring>
#include <mutex>
#include <string>
#include <unordered_map>
#include <vector>

namespace {

typedef unsigned int GLenum;
typedef int GLint;
typedef unsigned int GLuint;
typedef int GLsizei;
typedef XID GLXDrawable;

constexpr GLenum kGlBack = 0x0405;
constexpr GLenum kGlReadBuffer = 0x0C02;
constexpr GLenum kGlPackAlignment = 0x0D05;
constexpr GLenum kGlPackRowLength = 0x0D02;
constexpr GLenum kGlPackSkipRows = 0x0D03;
constexpr GLenum kGlPackSkipPixels = 0x0D04;
constexpr GLenum kGlBgra = 0x80E1;
constexpr GLenum kGlUnsignedByte = 0x1401;
constexpr GLenum kGlPixelPackBuffer = 0x88EB;
constexpr GLenum kGlPixelPackBufferBinding = 0x88ED;
constexpr GLenum kGlReadFramebuffer = 0x8CA8;
constexpr GLenum kGlReadFramebufferBinding = 0x8CAA;

enum InstallStatus {
    kInstalled = 0,
    kNoOpengl = 1,
    kNoX11 = 2,
};

typedef void (*SwapBuffersFn)(Display*, GLXDrawable);
typedef void (*GetIntegervFn)(GLenum, GLint*);
typedef void (*EnumFn)(GLenum);
typedef void (*PixelStoreiFn)(GLenum, GLint);
typedef void (*ReadPixelsFn)(GLint, GLint, GLsizei, GLsizei, GLenum, GLenum, void*);
typedef void (*BindFn)(GLenum, GLuint);
typedef void* (*GetProcAddressFn)(const unsigned char*);
typedef int (*GetGeometryFn)(Display*, Drawable, Window*, int*, int*, unsigned int*, unsigned int*,
                             unsigned int*, unsigned int*);
typedef jboolean (JNICALL* GetAwtFn)(JNIEnv*, JAWT*);

struct Gl {
    SwapBuffersFn swap_buffers;
    GetIntegervFn get_integerv;
    EnumFn read_buffer;
    PixelStoreiFn pixel_storei;
    ReadPixelsFn read_pixels;
    BindFn bind_framebuffer;
    BindFn bind_buffer;
    GetGeometryFn get_geometry;
};

struct Frame {
    int width = 0;
    int height = 0;
    uint64_t sequence = 0;
    std::vector<uint32_t> pixels;
};

std::mutex g_lock;
int g_status = -1;
Gl g_gl = {};
std::unordered_map<GLXDrawable, Frame> g_frames;
// The process's load count when the imports were last rebound; Track is on the event thread only.
unsigned long long g_rebound_loads = 0;

// Reads the back buffer of |drawable| into its frame, 0xFFRRGGBB top row first, restoring every
// piece of state the read touches.
void ReadBack(Display* display, GLXDrawable drawable) {
    {
        std::lock_guard<std::mutex> guard(g_lock);
        if (g_frames.find(drawable) == g_frames.end()) return;
    }
    Window root;
    int x, y;
    unsigned int width = 0, height = 0, border, depth;
    if (!g_gl.get_geometry(display, drawable, &root, &x, &y, &width, &height, &border, &depth)) return;
    if (width == 0 || height == 0) return;

    GLint read_buffer = 0, read_framebuffer = 0, pack_buffer = 0;
    GLint alignment = 4, row_length = 0, skip_rows = 0, skip_pixels = 0;
    g_gl.get_integerv(kGlReadBuffer, &read_buffer);
    g_gl.get_integerv(kGlPackAlignment, &alignment);
    g_gl.get_integerv(kGlPackRowLength, &row_length);
    g_gl.get_integerv(kGlPackSkipRows, &skip_rows);
    g_gl.get_integerv(kGlPackSkipPixels, &skip_pixels);
    if (g_gl.bind_framebuffer) g_gl.get_integerv(kGlReadFramebufferBinding, &read_framebuffer);
    if (g_gl.bind_buffer) g_gl.get_integerv(kGlPixelPackBufferBinding, &pack_buffer);
    if (read_framebuffer != 0) g_gl.bind_framebuffer(kGlReadFramebuffer, 0);
    if (pack_buffer != 0) g_gl.bind_buffer(kGlPixelPackBuffer, 0);
    g_gl.read_buffer(kGlBack);
    g_gl.pixel_storei(kGlPackAlignment, 4);
    g_gl.pixel_storei(kGlPackRowLength, 0);
    g_gl.pixel_storei(kGlPackSkipRows, 0);
    g_gl.pixel_storei(kGlPackSkipPixels, 0);

    std::vector<uint32_t> bottom_up(static_cast<size_t>(width) * height);
    g_gl.read_pixels(0, 0, static_cast<GLsizei>(width), static_cast<GLsizei>(height), kGlBgra, kGlUnsignedByte,
                     bottom_up.data());

    g_gl.pixel_storei(kGlPackSkipPixels, skip_pixels);
    g_gl.pixel_storei(kGlPackSkipRows, skip_rows);
    g_gl.pixel_storei(kGlPackRowLength, row_length);
    g_gl.pixel_storei(kGlPackAlignment, alignment);
    g_gl.read_buffer(static_cast<GLenum>(read_buffer));
    if (pack_buffer != 0) g_gl.bind_buffer(kGlPixelPackBuffer, static_cast<GLuint>(pack_buffer));
    if (read_framebuffer != 0) g_gl.bind_framebuffer(kGlReadFramebuffer, static_cast<GLuint>(read_framebuffer));

    // The back buffer's alpha is whatever the scene left there; the window shows it opaque.
    std::vector<uint32_t> top_down(bottom_up.size());
    for (unsigned int row = 0; row < height; row++) {
        const uint32_t* src = bottom_up.data() + static_cast<size_t>(height - 1 - row) * width;
        uint32_t* dst = top_down.data() + static_cast<size_t>(row) * width;
        for (unsigned int col = 0; col < width; col++) dst[col] = src[col] | 0xFF000000u;
    }

    std::lock_guard<std::mutex> guard(g_lock);
    auto it = g_frames.find(drawable);
    if (it == g_frames.end()) return;
    it->second.width = static_cast<int>(width);
    it->second.height = static_cast<int>(height);
    it->second.pixels.swap(top_down);
    it->second.sequence++;
}

void SwapBuffersDetour(Display* display, GLXDrawable drawable) {
    ReadBack(display, drawable);
    g_gl.swap_buffers(display, drawable);
}

bool IsOpenglLibrary(const char* name) {
    const char* base = std::strrchr(name, '/');
    base = base ? base + 1 : name;
    return std::strncmp(base, "libGL.", 6) == 0 || std::strncmp(base, "libGLX", 6) == 0 ||
           std::strncmp(base, "libGLdispatch", 13) == 0;
}

// Points every glXSwapBuffers import of one loaded object at the detour.
int RebindObject(struct dl_phdr_info* info, size_t, void*) {
    if (info->dlpi_name == nullptr || IsOpenglLibrary(info->dlpi_name)) return 0;
    const ElfW(Dyn)* dynamic = nullptr;
    for (int i = 0; i < info->dlpi_phnum; i++) {
        if (info->dlpi_phdr[i].p_type == PT_DYNAMIC) {
            dynamic = reinterpret_cast<const ElfW(Dyn)*>(info->dlpi_addr + info->dlpi_phdr[i].p_vaddr);
        }
    }
    if (dynamic == nullptr) return 0;
    const ElfW(Sym)* symbols = nullptr;
    const char* strings = nullptr;
    const ElfW(Rela)* tables[2] = {nullptr, nullptr};
    size_t sizes[2] = {0, 0};
    for (; dynamic->d_tag != DT_NULL; dynamic++) {
        switch (dynamic->d_tag) {
            case DT_SYMTAB: symbols = reinterpret_cast<const ElfW(Sym)*>(dynamic->d_un.d_ptr); break;
            case DT_STRTAB: strings = reinterpret_cast<const char*>(dynamic->d_un.d_ptr); break;
            case DT_JMPREL: tables[0] = reinterpret_cast<const ElfW(Rela)*>(dynamic->d_un.d_ptr); break;
            case DT_PLTRELSZ: sizes[0] = dynamic->d_un.d_val; break;
            case DT_RELA: tables[1] = reinterpret_cast<const ElfW(Rela)*>(dynamic->d_un.d_ptr); break;
            case DT_RELASZ: sizes[1] = dynamic->d_un.d_val; break;
        }
    }
    if (symbols == nullptr || strings == nullptr) return 0;
    const uintptr_t page = static_cast<uintptr_t>(sysconf(_SC_PAGESIZE));
    for (int t = 0; t < 2; t++) {
        if (tables[t] == nullptr) continue;
        for (size_t k = 0; k < sizes[t] / sizeof(ElfW(Rela)); k++) {
            const ElfW(Rela)& relocation = tables[t][k];
            const unsigned type = ELF64_R_TYPE(relocation.r_info);
            if (type != R_X86_64_JUMP_SLOT && type != R_X86_64_GLOB_DAT) continue;
            if (std::strcmp(strings + symbols[ELF64_R_SYM(relocation.r_info)].st_name, "glXSwapBuffers") != 0) continue;
            void** slot = reinterpret_cast<void**>(info->dlpi_addr + relocation.r_offset);
            if (*slot == reinterpret_cast<void*>(&SwapBuffersDetour)) continue;
            // RELRO leaves an eagerly bound slot read-only.
            void* start = reinterpret_cast<void*>(reinterpret_cast<uintptr_t>(slot) & ~(page - 1));
            if (mprotect(start, page, PROT_READ | PROT_WRITE) != 0) continue;
            *slot = reinterpret_cast<void*>(&SwapBuffersDetour);
        }
    }
    return 0;
}

// How many objects the process has loaded so far, which the first object reports; stops there.
int CountLoads(struct dl_phdr_info* info, size_t, void* loads) {
    *static_cast<unsigned long long*>(loads) = info->dlpi_adds;
    return 1;
}

int Install() {
    void* gl = dlopen("libGL.so.1", RTLD_NOW | RTLD_GLOBAL);
    void* x11 = dlopen("libX11.so.6", RTLD_NOW | RTLD_GLOBAL);
    if (gl == nullptr) return kNoOpengl;
    if (x11 == nullptr) return kNoX11;
    Gl resolved = {};
    resolved.swap_buffers = reinterpret_cast<SwapBuffersFn>(dlsym(gl, "glXSwapBuffers"));
    resolved.get_integerv = reinterpret_cast<GetIntegervFn>(dlsym(gl, "glGetIntegerv"));
    resolved.read_buffer = reinterpret_cast<EnumFn>(dlsym(gl, "glReadBuffer"));
    resolved.pixel_storei = reinterpret_cast<PixelStoreiFn>(dlsym(gl, "glPixelStorei"));
    resolved.read_pixels = reinterpret_cast<ReadPixelsFn>(dlsym(gl, "glReadPixels"));
    resolved.get_geometry = reinterpret_cast<GetGeometryFn>(dlsym(x11, "XGetGeometry"));
    auto get_proc = reinterpret_cast<GetProcAddressFn>(dlsym(gl, "glXGetProcAddressARB"));
    if (get_proc != nullptr) {
        resolved.bind_framebuffer =
            reinterpret_cast<BindFn>(get_proc(reinterpret_cast<const unsigned char*>("glBindFramebuffer")));
        resolved.bind_buffer =
            reinterpret_cast<BindFn>(get_proc(reinterpret_cast<const unsigned char*>("glBindBuffer")));
    }
    if (!resolved.swap_buffers || !resolved.get_integerv || !resolved.read_buffer || !resolved.pixel_storei ||
        !resolved.read_pixels) {
        return kNoOpengl;
    }
    if (!resolved.get_geometry) return kNoX11;
    g_gl = resolved;
    return kInstalled;
}

// JAWT_GetAWT from the libjawt.so next to the libawt.so this JVM loaded: the JVM's lib directory is
// not on every launcher's library path.
GetAwtFn ResolveGetAwt() {
    void* awt = dlopen("libawt.so", RTLD_NOW | RTLD_NOLOAD);
    if (awt == nullptr) return nullptr;
    Dl_info info;
    void* symbol = dlsym(awt, "JNI_OnLoad");
    if (symbol == nullptr || dladdr(symbol, &info) == 0 || info.dli_fname == nullptr) return nullptr;
    std::string path(info.dli_fname);
    const size_t slash = path.rfind('/');
    if (slash == std::string::npos) return nullptr;
    path.replace(slash + 1, std::string::npos, "libjawt.so");
    void* jawt = dlopen(path.c_str(), RTLD_NOW);
    return jawt == nullptr ? nullptr : reinterpret_cast<GetAwtFn>(dlsym(jawt, "JAWT_GetAWT"));
}

GLXDrawable WindowOf(JNIEnv* env, jobject component) {
    static const GetAwtFn get_awt = ResolveGetAwt();
    JAWT awt;
    awt.version = JAWT_VERSION_1_4;
    if (get_awt == nullptr || !get_awt(env, &awt)) return 0;
    JAWT_DrawingSurface* surface = awt.GetDrawingSurface(env, component);
    if (surface == nullptr) return 0;
    GLXDrawable window = 0;
    if ((surface->Lock(surface) & JAWT_LOCK_ERROR) == 0) {
        JAWT_DrawingSurfaceInfo* info = surface->GetDrawingSurfaceInfo(surface);
        if (info != nullptr) {
            window = static_cast<JAWT_X11DrawingSurfaceInfo*>(info->platformInfo)->drawable;
            surface->FreeDrawingSurfaceInfo(info);
        }
        surface->Unlock(surface);
    }
    awt.FreeDrawingSurface(surface);
    return window;
}

}  // namespace

extern "C" {

// Resolves OpenGL once per process and returns the outcome (0 = installed); later calls return the
// first outcome.
JNIEXPORT jint JNICALL Java_dev_equo_swt_awt_GlPresentHook_Install(JNIEnv* env, jclass cls) {
    std::lock_guard<std::mutex> guard(g_lock);
    if (g_status < 0) g_status = Install();
    return g_status;
}

// Starts reading back the frames |canvas|'s window presents and returns that window; 0 when it has
// none. Rebinds the objects loaded since the last call too, the canvas's own library among them.
JNIEXPORT jlong JNICALL Java_dev_equo_swt_awt_GlPresentHook_Track(JNIEnv* env, jclass cls, jobject canvas) {
    if (g_status != kInstalled) return 0;
    GLXDrawable window = WindowOf(env, canvas);
    if (window == 0) return 0;
    {
        std::lock_guard<std::mutex> guard(g_lock);
        g_frames.emplace(window, Frame{});
    }
    unsigned long long loads = 0;
    dl_iterate_phdr(&CountLoads, &loads);
    if (loads != g_rebound_loads) {
        dl_iterate_phdr(&RebindObject, nullptr);
        g_rebound_loads = loads;
    }
    return static_cast<jlong>(window);
}

// Stops reading back |window|'s frames, once its canvas has another window or none.
JNIEXPORT void JNICALL Java_dev_equo_swt_awt_GlPresentHook_Untrack(JNIEnv* env, jclass cls, jlong window) {
    std::lock_guard<std::mutex> guard(g_lock);
    g_frames.erase(static_cast<GLXDrawable>(window));
}

// [width, height, pixels...] of the last frame |window| presented, 0xFFRRGGBB top row first; null
// while there is none.
JNIEXPORT jintArray JNICALL Java_dev_equo_swt_awt_GlPresentHook_Frame(JNIEnv* env, jclass cls, jlong window) {
    std::lock_guard<std::mutex> guard(g_lock);
    auto it = g_frames.find(static_cast<GLXDrawable>(window));
    if (it == g_frames.end() || it->second.pixels.empty()) return nullptr;
    const jsize count = static_cast<jsize>(it->second.pixels.size());
    jintArray result = env->NewIntArray(2 + count);
    if (result == nullptr) return nullptr;
    const jint size[] = {it->second.width, it->second.height};
    env->SetIntArrayRegion(result, 0, 2, size);
    env->SetIntArrayRegion(result, 2, count, reinterpret_cast<const jint*>(it->second.pixels.data()));
    return result;
}

}  // extern "C"
