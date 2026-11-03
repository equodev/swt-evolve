// Keeps the front-buffer drawing of a tracked canvas's hidden drawables in a layer of its own and
// reads that layer back when the drawing is flushed (dev.equo.swt.awt.GlFrontBuffer).
// Applications call opengl32 directly, so glDrawBuffer, glClear and glFlush are patched in place with
// a jump and a trampoline.

#include <windows.h>
#include <tlhelp32.h>
#include <jni.h>
#include <jawt.h>
#include <jawt_md.h>

#include <climits>
#include <cstdint>
#include <cstring>
#include <cwchar>
#include <vector>

namespace {

typedef void(WINAPI* DrawBufferFn)(unsigned int mode);
typedef void(WINAPI* FlushFn)();
typedef void(WINAPI* GetIntegervFn)(unsigned int name, int* params);
typedef HDC(WINAPI* GetCurrentDcFn)();
typedef void(WINAPI* BitfieldFn)(unsigned int mask);
typedef void(WINAPI* VoidFn)();
typedef void(WINAPI* EnumFn)(unsigned int value);
typedef void(WINAPI* ColorMaskFn)(unsigned char red, unsigned char green, unsigned char blue, unsigned char alpha);
typedef void(WINAPI* ClearColorFn)(float red, float green, float blue, float alpha);
typedef void(WINAPI* PixelStoreiFn)(unsigned int name, int value);
typedef void(WINAPI* ReadPixelsFn)(int x, int y, int width, int height, unsigned int format, unsigned int type,
                                   void* pixels);
typedef void(WINAPI* DrawPixelsFn)(int width, int height, unsigned int format, unsigned int type, const void* pixels);
typedef void(WINAPI* RasterPos2iFn)(int x, int y);
typedef void(WINAPI* PixelZoomFn)(float x, float y);
typedef jboolean(JNICALL* GetAwtFn)(JNIEnv* env, JAWT* awt);

constexpr unsigned int kGlFrontLeft = 0x0400;
constexpr unsigned int kGlFront = 0x0404;
constexpr unsigned int kGlBack = 0x0405;
constexpr unsigned int kGlReadBuffer = 0x0C02;
constexpr unsigned int kGlDoubleBuffer = 0x0C32;
constexpr unsigned int kGlScissorTest = 0x0C11;
constexpr unsigned int kGlColorBufferBit = 0x4000;
constexpr unsigned int kGlScissorBit = 0x80000;
constexpr unsigned int kGlClientPixelStoreBit = 0x1;
constexpr unsigned int kGlPackSwapBytes = 0x0D00;
constexpr unsigned int kGlPackLsbFirst = 0x0D01;
constexpr unsigned int kGlPackRowLength = 0x0D02;
constexpr unsigned int kGlPackSkipRows = 0x0D03;
constexpr unsigned int kGlPackSkipPixels = 0x0D04;
constexpr unsigned int kGlPackAlignment = 0x0D05;
constexpr unsigned int kGlPixelPackBufferBinding = 0x88ED;
constexpr unsigned int kGlBgra = 0x80E1;
constexpr unsigned int kGlUnsignedByte = 0x1401;
constexpr unsigned int kGlViewport = 0x0BA2;
constexpr unsigned int kGlAllAttribBits = 0x000FFFFF;
constexpr unsigned int kGlUnpackSwapBytes = 0x0CF0;
constexpr unsigned int kGlUnpackLsbFirst = 0x0CF1;
constexpr unsigned int kGlUnpackRowLength = 0x0CF2;
constexpr unsigned int kGlUnpackSkipRows = 0x0CF3;
constexpr unsigned int kGlUnpackSkipPixels = 0x0CF4;
constexpr unsigned int kGlUnpackAlignment = 0x0CF5;
constexpr unsigned int kGlPixelUnpackBufferBinding = 0x88EF;
constexpr unsigned int kGlCurrentProgram = 0x8B8D;
constexpr unsigned int kGlModelview = 0x1700;
constexpr unsigned int kGlProjection = 0x1701;
// What applies to the fragments of glDrawPixels and is switched off to copy pixels as they are.
constexpr unsigned int kGlPixelCopyDisables[] = {
    0x0BE2,  // GL_BLEND
    0x0BF2,  // GL_COLOR_LOGIC_OP
    0x0B71,  // GL_DEPTH_TEST
    0x0B90,  // GL_STENCIL_TEST
    0x0BC0,  // GL_ALPHA_TEST
    0x0C11,  // GL_SCISSOR_TEST
    0x0B60,  // GL_FOG
    0x0BD0,  // GL_DITHER
    0x0DE0,  // GL_TEXTURE_1D
    0x0DE1,  // GL_TEXTURE_2D
};

constexpr size_t kJumpSize = 5;
constexpr size_t kAbsoluteJumpSize = 14;
constexpr size_t kTrampolineSize = 64;

enum Status {
    kInstalled = 0,
    kNoOpengl = 1,
    kDrawBufferNotPatchable = 2,
    kFlushNotPatchable = 3,
    kNoMemoryNearOpengl = 4,
    kNotWritable = 5,
    kClearNotPatchable = 6,
};

// The opengl32 entry points used; draw_buffer and flush are the trampolines to the original code.
struct Gl {
    DrawBufferFn draw_buffer;
    FlushFn flush;
    GetIntegervFn get_integerv;
    GetCurrentDcFn get_current_dc;
    BitfieldFn push_attrib;
    VoidFn pop_attrib;
    BitfieldFn push_client_attrib;
    VoidFn pop_client_attrib;
    EnumFn disable;
    ColorMaskFn color_mask;
    ClearColorFn clear_color;
    BitfieldFn clear;
    EnumFn read_buffer;
    PixelStoreiFn pixel_storei;
    ReadPixelsFn read_pixels;
    DrawPixelsFn draw_pixels;
    EnumFn matrix_mode;
    VoidFn push_matrix;
    VoidFn pop_matrix;
    VoidFn load_identity;
    RasterPos2iFn raster_pos_2i;
    PixelZoomFn pixel_zoom;
};

Gl g_gl = {};

// A tracked canvas's overlay layer, the buffer its front-buffer drawing goes to: the back buffer of
// the canvas's own hidden window, the drawing being sent there, or the front buffer of the drawable
// its scene renders into (|scene_dc|, learnt while the host paints the canvas), which is never
// presented. |layer_dc| and |layer_buffer| say which one was drawn into last. |stale| asks for the
// layer to be cleared before it is drawn into again; |overwritten| for it to get |pixels| back, as
// something else drew into the window; |drawn| whether it was drawn into since the scene was last
// rendered; |pixels| holds what the last flush read, top row first, empty while there is none.
struct Overlay {
    HWND window;
    HDC scene_dc;
    HDC layer_dc;
    unsigned int layer_buffer;
    bool stale;
    bool overwritten;
    bool drawn;
    int width;
    int height;
    std::vector<uint32_t> pixels;
};

SRWLOCK g_lock = SRWLOCK_INIT;
std::vector<Overlay> g_overlays;
HANDLE g_flushed = nullptr;
int g_status = -1;

thread_local HWND t_redirected = nullptr;
thread_local unsigned int t_redirected_layer = kGlBack;
// The tracked window whose canvas this thread is painting for the host, when its scene renders
// show which drawable they go to.
thread_local HWND t_painting = nullptr;
thread_local std::vector<uint32_t> t_rows;

// Callers hold g_lock.
Overlay* Find(HWND window) {
    for (Overlay& overlay : g_overlays) {
        if (overlay.window == window) return &overlay;
    }
    return nullptr;
}

// The overlay of the canvas that |dc| (whose window is |window|) belongs to, and the buffer that
// holds it there. Callers hold g_lock.
Overlay* OverlayOf(HDC dc, HWND window, unsigned int* layer) {
    for (Overlay& overlay : g_overlays) {
        if (window != nullptr && overlay.window == window) {
            *layer = kGlBack;
            return &overlay;
        }
    }
    for (Overlay& overlay : g_overlays) {
        if (overlay.scene_dc == dc) {
            *layer = kGlFront;
            return &overlay;
        }
    }
    return nullptr;
}

// Clears |buffer| to black and leaves the state it touches as it found it.
void ClearLayer(unsigned int buffer) {
    g_gl.push_attrib(kGlColorBufferBit | kGlScissorBit);
    g_gl.draw_buffer(buffer);
    g_gl.disable(kGlScissorTest);
    g_gl.color_mask(1, 1, 1, 1);
    g_gl.clear_color(0, 0, 0, 0);
    g_gl.clear(kGlColorBufferBit);
    g_gl.pop_attrib();
}

// Draws |pixels| (top row first, as read back) into |buffer| from its bottom-left corner, as they
// are, and leaves the state it touches as it found it. False when that cannot be done here.
bool RestoreLayer(unsigned int buffer, const std::vector<uint32_t>& pixels, int width, int height) {
    int viewport[4] = {0, 0, 0, 0};
    g_gl.get_integerv(kGlViewport, viewport);
    if (viewport[0] != 0 || viewport[1] != 0 || viewport[2] != width || viewport[3] != height) return false;
    int program = 0, unpack_buffer = 0;
    g_gl.get_integerv(kGlCurrentProgram, &program);
    g_gl.get_integerv(kGlPixelUnpackBufferBinding, &unpack_buffer);
    if (program != 0 || unpack_buffer != 0) return false;
    t_rows.resize(pixels.size());
    const size_t row_bytes = static_cast<size_t>(width) * sizeof(uint32_t);
    for (int y = 0; y < height; y++) {
        std::memcpy(&t_rows[static_cast<size_t>(height - 1 - y) * width], &pixels[static_cast<size_t>(y) * width],
                    row_bytes);
    }
    g_gl.push_attrib(kGlAllAttribBits);
    g_gl.push_client_attrib(kGlClientPixelStoreBit);
    g_gl.draw_buffer(buffer);
    for (unsigned int cap : kGlPixelCopyDisables) g_gl.disable(cap);
    g_gl.color_mask(1, 1, 1, 1);
    g_gl.pixel_zoom(1, 1);
    g_gl.pixel_storei(kGlUnpackSwapBytes, 0);
    g_gl.pixel_storei(kGlUnpackLsbFirst, 0);
    g_gl.pixel_storei(kGlUnpackRowLength, 0);
    g_gl.pixel_storei(kGlUnpackSkipRows, 0);
    g_gl.pixel_storei(kGlUnpackSkipPixels, 0);
    g_gl.pixel_storei(kGlUnpackAlignment, 4);
    g_gl.matrix_mode(kGlProjection);
    g_gl.push_matrix();
    g_gl.load_identity();
    g_gl.matrix_mode(kGlModelview);
    g_gl.push_matrix();
    g_gl.load_identity();
    g_gl.raster_pos_2i(-1, -1);
    g_gl.draw_pixels(width, height, kGlBgra, kGlUnsignedByte, t_rows.data());
    g_gl.pop_matrix();
    g_gl.matrix_mode(kGlProjection);
    g_gl.pop_matrix();
    g_gl.pop_client_attrib();
    g_gl.pop_attrib();
    return true;
}

// Reads |layer|, which holds nothing but the overlay of |window|'s canvas, into its Overlay.
void ReadBack(HWND window, unsigned int layer) {
    int viewport[4] = {0, 0, 0, 0};
    g_gl.get_integerv(kGlViewport, viewport);
    const int width = viewport[0] + viewport[2];
    const int height = viewport[1] + viewport[3];
    if (width <= 0 || height <= 0) return;
    int pack_buffer = 0;
    g_gl.get_integerv(kGlPixelPackBufferBinding, &pack_buffer);
    if (pack_buffer != 0) return;
    int read_buffer = static_cast<int>(kGlBack);
    g_gl.get_integerv(kGlReadBuffer, &read_buffer);
    t_rows.resize(static_cast<size_t>(width) * height);
    g_gl.push_client_attrib(kGlClientPixelStoreBit);
    g_gl.pixel_storei(kGlPackSwapBytes, 0);
    g_gl.pixel_storei(kGlPackLsbFirst, 0);
    g_gl.pixel_storei(kGlPackRowLength, 0);
    g_gl.pixel_storei(kGlPackSkipRows, 0);
    g_gl.pixel_storei(kGlPackSkipPixels, 0);
    g_gl.pixel_storei(kGlPackAlignment, 4);
    g_gl.read_buffer(layer);
    g_gl.read_pixels(0, 0, width, height, kGlBgra, kGlUnsignedByte, t_rows.data());
    g_gl.read_buffer(static_cast<unsigned int>(read_buffer));
    g_gl.pop_client_attrib();

    ::AcquireSRWLockExclusive(&g_lock);
    Overlay* overlay = Find(window);
    if (overlay != nullptr && !overlay->stale) {
        overlay->width = width;
        overlay->height = height;
        overlay->pixels.resize(t_rows.size());
        const size_t row_bytes = static_cast<size_t>(width) * sizeof(uint32_t);
        for (int y = 0; y < height; y++) {
            std::memcpy(&overlay->pixels[static_cast<size_t>(y) * width],
                        &t_rows[static_cast<size_t>(height - 1 - y) * width], row_bytes);
        }
    }
    ::ReleaseSRWLockExclusive(&g_lock);
}

// A new frame rendered into |dc|, which starts by clearing its colour buffer. While the host paints a
// canvas, that is the drawable its scene goes to. A frame of a canvas's scene wipes its overlay, as
// it wipes front-buffer drawing on screen, so the layer is cleared before the next drawing into it.
// The application may render on its own, outside the host's paint, so this cannot wait for one.
// Anything drawn into the canvas's own window instead is no frame the application counts as wiping
// its overlay, which it then erases by drawing it again, so the layer gets its last pixels back.
void Rendered(HDC dc) {
    if (dc == nullptr) return;
    HWND window = ::WindowFromDC(dc);
    ::AcquireSRWLockExclusive(&g_lock);
    // The canvas's own window holds the overlay, never the scene of an off-screen canvas.
    if (t_painting != nullptr && window != t_painting) {
        Overlay* painting = Find(t_painting);
        if (painting != nullptr) painting->scene_dc = dc;
    }
    for (Overlay& overlay : g_overlays) {
        if (overlay.scene_dc == dc) {
            overlay.stale = true;
            if (overlay.drawn) {
                overlay.drawn = false;
                overlay.pixels.clear();
            }
        } else if (window != nullptr && overlay.window == window) {
            overlay.overwritten = true;
        }
    }
    ::ReleaseSRWLockExclusive(&g_lock);
}

// Sends a front-buffer drawing on a tracked canvas's hidden drawable to its overlay layer.
void DrawFront(unsigned int* mode) {
    HDC dc = g_gl.get_current_dc();
    if (dc == nullptr) return;
    HWND window = ::WindowFromDC(dc);
    if (window != nullptr && ::IsWindowVisible(window)) return;
    int double_buffered = 0;
    g_gl.get_integerv(kGlDoubleBuffer, &double_buffered);
    if (double_buffered == 0) return;
    unsigned int layer = kGlBack;
    HWND owner = nullptr;
    bool clear = false;
    std::vector<uint32_t> restore;
    int width = 0, height = 0;
    ::AcquireSRWLockExclusive(&g_lock);
    Overlay* overlay = OverlayOf(dc, window, &layer);
    if (overlay != nullptr) {
        owner = overlay->window;
        clear = overlay->stale || overlay->layer_dc != dc || overlay->layer_buffer != layer;
        if (!clear && overlay->overwritten && layer == kGlBack) {
            restore = overlay->pixels;
            width = overlay->width;
            height = overlay->height;
            clear = true;
        }
        overlay->stale = false;
        overlay->overwritten = false;
        overlay->drawn = true;
        overlay->layer_dc = dc;
        overlay->layer_buffer = layer;
    }
    ::ReleaseSRWLockExclusive(&g_lock);
    if (owner == nullptr) return;
    if (clear) ClearLayer(layer);
    // Left cleared when it cannot be restored: the drawing erased next then shows once more.
    if (!restore.empty()) RestoreLayer(layer, restore, width, height);
    *mode = layer;
    t_redirected = owner;
    t_redirected_layer = layer;
}

void WINAPI DrawBufferDetour(unsigned int mode) {
    if (mode == kGlFront || mode == kGlFrontLeft) DrawFront(&mode);
    g_gl.draw_buffer(mode);
}

void WINAPI ClearDetour(unsigned int mask) {
    if ((mask & kGlColorBufferBit) != 0) Rendered(g_gl.get_current_dc());
    g_gl.clear(mask);
}

void WINAPI FlushDetour() {
    g_gl.flush();
    HWND window = t_redirected;
    if (window == nullptr) return;
    t_redirected = nullptr;
    ReadBack(window, t_redirected_layer);
    ::SetEvent(g_flushed);
}

// JAWT_GetAWT from the jawt.dll next to the awt.dll this JVM loaded: the JVM's directory is not on
// every launcher's DLL search path.
GetAwtFn ResolveGetAwt() {
    HMODULE awt = ::GetModuleHandleW(L"awt.dll");
    if (awt == nullptr) return nullptr;
    wchar_t path[MAX_PATH];
    const DWORD length = ::GetModuleFileNameW(awt, path, MAX_PATH);
    if (length == 0 || length >= MAX_PATH) return nullptr;
    wchar_t* name = std::wcsrchr(path, L'\\');
    if (name == nullptr) return nullptr;
    const size_t room = MAX_PATH - static_cast<size_t>(name + 1 - path);
    if (wcscpy_s(name + 1, room, L"jawt.dll") != 0) return nullptr;
    HMODULE jawt = ::LoadLibraryW(path);
    return jawt == nullptr ? nullptr : reinterpret_cast<GetAwtFn>(::GetProcAddress(jawt, "JAWT_GetAWT"));
}

HWND WindowOf(JNIEnv* env, jobject component) {
    static const GetAwtFn get_awt = ResolveGetAwt();
    JAWT awt;
    awt.version = JAWT_VERSION_1_4;
    if (get_awt == nullptr || !get_awt(env, &awt)) return nullptr;
    JAWT_DrawingSurface* surface = awt.GetDrawingSurface(env, component);
    if (surface == nullptr) return nullptr;
    HWND window = nullptr;
    if ((surface->Lock(surface) & JAWT_LOCK_ERROR) == 0) {
        JAWT_DrawingSurfaceInfo* info = surface->GetDrawingSurfaceInfo(surface);
        if (info != nullptr) {
            window = static_cast<JAWT_Win32DrawingSurfaceInfo*>(info->platformInfo)->hwnd;
            surface->FreeDrawingSurfaceInfo(info);
        }
        surface->Unlock(surface);
    }
    awt.FreeDrawingSurface(surface);
    return window;
}

// Length of one instruction of the kind a compiler puts in a function prologue, and where its
// RIP-relative displacement sits (-1 for none). 0 for anything else, branches included.
size_t PrologueInstruction(const uint8_t* p, int* rip_displacement_at) {
    *rip_displacement_at = -1;
    size_t i = 0;
    if ((p[i] & 0xF0) == 0x40) i++;
    const uint8_t op = p[i++];
    if (op >= 0x50 && op <= 0x57) return i;
    size_t immediate = 0;
    switch (op) {
        case 0x83: immediate = 1; break;
        case 0x81: immediate = 4; break;
        case 0x03: case 0x2B: case 0x33: case 0x85: case 0x89: case 0x8B: case 0x8D: break;
        default: return 0;
    }
    const uint8_t modrm = p[i++];
    const uint8_t mod = modrm >> 6;
    const uint8_t rm = modrm & 7;
    if (mod != 3 && rm == 4) {
        const uint8_t sib = p[i++];
        if (mod == 0 && (sib & 7) == 5) i += 4;
    } else if (mod == 0 && rm == 5) {
        *rip_displacement_at = static_cast<int>(i);
        i += 4;
    }
    if (mod == 1) i += 1;
    if (mod == 2) i += 4;
    return i + immediate;
}

// Copies whole instructions from |src| to |dst| until at least |need| bytes are covered, re-aiming
// RIP-relative operands. Returns the bytes copied, or 0 when the prologue cannot be moved.
size_t CopyPrologue(const uint8_t* src, uint8_t* dst, size_t need) {
    size_t copied = 0;
    while (copied < need) {
        int displacement_at;
        const size_t length = PrologueInstruction(src + copied, &displacement_at);
        if (length == 0) return 0;
        std::memcpy(dst + copied, src + copied, length);
        if (displacement_at >= 0) {
            int32_t displacement;
            std::memcpy(&displacement, src + copied + displacement_at, sizeof(displacement));
            const int64_t target = reinterpret_cast<int64_t>(src + copied + length) + displacement;
            const int64_t moved = target - reinterpret_cast<int64_t>(dst + copied + length);
            if (moved < INT32_MIN || moved > INT32_MAX) return 0;
            const int32_t relocated = static_cast<int32_t>(moved);
            std::memcpy(dst + copied + displacement_at, &relocated, sizeof(relocated));
        }
        copied += length;
    }
    return copied;
}

void WriteAbsoluteJump(uint8_t* at, const void* to) {
    static const uint8_t kJmpRipIndirect[] = {0xFF, 0x25, 0x00, 0x00, 0x00, 0x00};
    std::memcpy(at, kJmpRipIndirect, sizeof(kJmpRipIndirect));
    const uint64_t address = reinterpret_cast<uint64_t>(to);
    std::memcpy(at + sizeof(kJmpRipIndirect), &address, sizeof(address));
}

// A page within a rel32 jump of |near|, where the trampolines and the jumps to the detours live.
uint8_t* AllocateNear(const void* near_address, size_t size) {
    SYSTEM_INFO info;
    ::GetSystemInfo(&info);
    const uintptr_t granularity = info.dwAllocationGranularity;
    const uintptr_t origin = reinterpret_cast<uintptr_t>(near_address) & ~(granularity - 1);
    const uintptr_t reach = 0x7FF00000;
    for (uintptr_t distance = granularity; distance < reach; distance += granularity) {
        const uintptr_t candidates[] = {origin - distance, origin + distance};
        for (uintptr_t at : candidates) {
            void* block = ::VirtualAlloc(reinterpret_cast<void*>(at), size, MEM_RESERVE | MEM_COMMIT,
                                         PAGE_EXECUTE_READWRITE);
            if (block != nullptr) return static_cast<uint8_t*>(block);
        }
    }
    return nullptr;
}

struct Hook {
    uint8_t* target;
    const void* detour;
    uint8_t* trampoline;
    uint8_t* relay;
    size_t copied;
};

// Builds the trampoline that runs |hook.target|'s first instructions and jumps back, and the jump
// to the detour. Touches nothing outside the allocated block.
bool Prepare(Hook& hook) {
    if (reinterpret_cast<uintptr_t>(hook.target) % 8 != 0) return false;
    hook.copied = CopyPrologue(hook.target, hook.trampoline, kJumpSize);
    if (hook.copied == 0 || hook.copied > kTrampolineSize - kAbsoluteJumpSize) return false;
    WriteAbsoluteJump(hook.trampoline + hook.copied, hook.target + hook.copied);
    WriteAbsoluteJump(hook.relay, hook.detour);
    return true;
}

constexpr size_t kPatchSize = 8;

// The other threads of this process, opened to be suspended and moved. Collected before any is
// suspended: a suspended thread may hold the heap lock.
std::vector<HANDLE> OtherThreads() {
    std::vector<DWORD> ids;
    HANDLE snapshot = ::CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD, 0);
    if (snapshot == INVALID_HANDLE_VALUE) return {};
    THREADENTRY32 entry = {sizeof(entry)};
    for (BOOL more = ::Thread32First(snapshot, &entry); more; more = ::Thread32Next(snapshot, &entry)) {
        if (entry.th32OwnerProcessID == ::GetCurrentProcessId() && entry.th32ThreadID != ::GetCurrentThreadId()) {
            ids.push_back(entry.th32ThreadID);
        }
    }
    ::CloseHandle(snapshot);
    std::vector<HANDLE> threads;
    threads.reserve(ids.size());
    for (DWORD id : ids) {
        HANDLE thread = ::OpenThread(THREAD_SUSPEND_RESUME | THREAD_GET_CONTEXT | THREAD_SET_CONTEXT, FALSE, id);
        if (thread != nullptr) threads.push_back(thread);
    }
    return threads;
}

// Points every hook's target at its detour, or leaves them all as they were. The other threads are
// suspended meanwhile, and one stopped inside a prologue being replaced resumes at the same
// instruction in its trampoline. A thread started after they were listed is not suspended, but it can
// only be entering a function, where it meets the old prologue or the jump: each target's first
// 8 bytes change in one aligned store.
bool PatchAll(Hook* hooks, size_t count) {
    DWORD protections[3];
    if (count > sizeof(protections) / sizeof(protections[0])) return false;
    for (size_t i = 0; i < count; i++) {
        if (!::VirtualProtect(hooks[i].target, kPatchSize, PAGE_EXECUTE_READWRITE, &protections[i])) {
            DWORD ignored;
            while (i-- > 0) ::VirtualProtect(hooks[i].target, kPatchSize, protections[i], &ignored);
            return false;
        }
    }

    std::vector<HANDLE> threads = OtherThreads();
    for (HANDLE thread : threads) ::SuspendThread(thread);
    for (HANDLE thread : threads) {
        CONTEXT context = {};
        context.ContextFlags = CONTEXT_CONTROL;
        if (!::GetThreadContext(thread, &context)) continue;
        for (size_t i = 0; i < count; i++) {
            const uintptr_t start = reinterpret_cast<uintptr_t>(hooks[i].target);
            if (context.Rip >= start && context.Rip < start + hooks[i].copied) {
                context.Rip = reinterpret_cast<uintptr_t>(hooks[i].trampoline) + (context.Rip - start);
                ::SetThreadContext(thread, &context);
                break;
            }
        }
    }
    for (size_t i = 0; i < count; i++) {
        const Hook& hook = hooks[i];
        uint8_t patch[kPatchSize];
        std::memcpy(patch, hook.target, sizeof(patch));
        patch[0] = 0xE9;
        const int32_t to_relay = static_cast<int32_t>(hook.relay - (hook.target + kJumpSize));
        std::memcpy(patch + 1, &to_relay, sizeof(to_relay));
        for (size_t j = kJumpSize; j < hook.copied && j < sizeof(patch); j++) patch[j] = 0xCC;
        int64_t value;
        std::memcpy(&value, patch, sizeof(value));
        ::InterlockedExchange64(reinterpret_cast<volatile LONG64*>(hook.target), value);
        ::FlushInstructionCache(::GetCurrentProcess(), hook.target, sizeof(patch));
    }
    for (HANDLE thread : threads) {
        ::ResumeThread(thread);
        ::CloseHandle(thread);
    }

    DWORD ignored;
    for (size_t i = 0; i < count; i++) ::VirtualProtect(hooks[i].target, kPatchSize, protections[i], &ignored);
    return true;
}

int Install() {
    HMODULE opengl = ::LoadLibraryW(L"opengl32.dll");
    if (opengl == nullptr) return kNoOpengl;
    auto* draw_buffer = reinterpret_cast<uint8_t*>(::GetProcAddress(opengl, "glDrawBuffer"));
    auto* flush = reinterpret_cast<uint8_t*>(::GetProcAddress(opengl, "glFlush"));
    Gl gl = {};
    gl.get_integerv = reinterpret_cast<GetIntegervFn>(::GetProcAddress(opengl, "glGetIntegerv"));
    gl.get_current_dc = reinterpret_cast<GetCurrentDcFn>(::GetProcAddress(opengl, "wglGetCurrentDC"));
    gl.push_attrib = reinterpret_cast<BitfieldFn>(::GetProcAddress(opengl, "glPushAttrib"));
    gl.pop_attrib = reinterpret_cast<VoidFn>(::GetProcAddress(opengl, "glPopAttrib"));
    gl.push_client_attrib = reinterpret_cast<BitfieldFn>(::GetProcAddress(opengl, "glPushClientAttrib"));
    gl.pop_client_attrib = reinterpret_cast<VoidFn>(::GetProcAddress(opengl, "glPopClientAttrib"));
    gl.disable = reinterpret_cast<EnumFn>(::GetProcAddress(opengl, "glDisable"));
    gl.color_mask = reinterpret_cast<ColorMaskFn>(::GetProcAddress(opengl, "glColorMask"));
    gl.clear_color = reinterpret_cast<ClearColorFn>(::GetProcAddress(opengl, "glClearColor"));
    gl.clear = reinterpret_cast<BitfieldFn>(::GetProcAddress(opengl, "glClear"));
    gl.read_buffer = reinterpret_cast<EnumFn>(::GetProcAddress(opengl, "glReadBuffer"));
    gl.pixel_storei = reinterpret_cast<PixelStoreiFn>(::GetProcAddress(opengl, "glPixelStorei"));
    gl.read_pixels = reinterpret_cast<ReadPixelsFn>(::GetProcAddress(opengl, "glReadPixels"));
    gl.draw_pixels = reinterpret_cast<DrawPixelsFn>(::GetProcAddress(opengl, "glDrawPixels"));
    gl.matrix_mode = reinterpret_cast<EnumFn>(::GetProcAddress(opengl, "glMatrixMode"));
    gl.push_matrix = reinterpret_cast<VoidFn>(::GetProcAddress(opengl, "glPushMatrix"));
    gl.pop_matrix = reinterpret_cast<VoidFn>(::GetProcAddress(opengl, "glPopMatrix"));
    gl.load_identity = reinterpret_cast<VoidFn>(::GetProcAddress(opengl, "glLoadIdentity"));
    gl.raster_pos_2i = reinterpret_cast<RasterPos2iFn>(::GetProcAddress(opengl, "glRasterPos2i"));
    gl.pixel_zoom = reinterpret_cast<PixelZoomFn>(::GetProcAddress(opengl, "glPixelZoom"));
    if (!draw_buffer || !flush || !gl.get_integerv || !gl.get_current_dc || !gl.push_attrib || !gl.pop_attrib ||
        !gl.push_client_attrib || !gl.pop_client_attrib || !gl.disable || !gl.color_mask || !gl.clear_color ||
        !gl.clear || !gl.read_buffer || !gl.pixel_storei || !gl.read_pixels || !gl.draw_pixels ||
        !gl.matrix_mode || !gl.push_matrix || !gl.pop_matrix || !gl.load_identity || !gl.raster_pos_2i ||
        !gl.pixel_zoom) {
        return kNoOpengl;
    }

    uint8_t* block = AllocateNear(draw_buffer, 4096);
    if (block == nullptr) return kNoMemoryNearOpengl;
    Hook draw_buffer_hook = {draw_buffer, reinterpret_cast<const void*>(&DrawBufferDetour), block,
                             block + 3 * kTrampolineSize, 0};
    Hook flush_hook = {flush, reinterpret_cast<const void*>(&FlushDetour), block + kTrampolineSize,
                       block + 4 * kTrampolineSize, 0};
    Hook clear_hook = {reinterpret_cast<uint8_t*>(gl.clear), reinterpret_cast<const void*>(&ClearDetour),
                       block + 2 * kTrampolineSize, block + 5 * kTrampolineSize, 0};
    const bool draw_buffer_ready = Prepare(draw_buffer_hook);
    const bool flush_ready = Prepare(flush_hook);
    const bool clear_ready = Prepare(clear_hook);
    if (!draw_buffer_ready || !flush_ready || !clear_ready) {
        ::VirtualFree(block, 0, MEM_RELEASE);
        if (!draw_buffer_ready) return kDrawBufferNotPatchable;
        return flush_ready ? kClearNotPatchable : kFlushNotPatchable;
    }

    g_flushed = ::CreateEventW(nullptr, FALSE, FALSE, nullptr);
    gl.draw_buffer = reinterpret_cast<DrawBufferFn>(draw_buffer_hook.trampoline);
    gl.flush = reinterpret_cast<FlushFn>(flush_hook.trampoline);
    gl.clear = reinterpret_cast<BitfieldFn>(clear_hook.trampoline);
    g_gl = gl;
    Hook hooks[] = {flush_hook, clear_hook, draw_buffer_hook};
    return PatchAll(hooks, sizeof(hooks) / sizeof(hooks[0])) ? kInstalled : kNotWritable;
}

}  // namespace

extern "C" {

// Patches opengl32 once per process and returns the outcome (0 = installed); later calls return
// the first outcome.
JNIEXPORT jint JNICALL Java_dev_equo_swt_awt_GlFrontBuffer_Install(JNIEnv* env, jclass cls) {
    if (g_status < 0) g_status = Install();
    return g_status;
}

// Blocks until a glFlush ends a redirected front-buffer draw. False when nothing was installed.
JNIEXPORT jboolean JNICALL Java_dev_equo_swt_awt_GlFrontBuffer_AwaitFrontFlush(JNIEnv* env, jclass cls) {
    if (g_flushed == nullptr) return JNI_FALSE;
    return ::WaitForSingleObject(g_flushed, INFINITE) == WAIT_OBJECT_0 ? JNI_TRUE : JNI_FALSE;
}

// Starts treating |canvas|'s window as an overlay layer and returns it; 0 when it has none.
JNIEXPORT jlong JNICALL Java_dev_equo_swt_awt_GlFrontBuffer_Track(JNIEnv* env, jclass cls, jobject canvas) {
    if (g_status != kInstalled) return 0;
    HWND window = WindowOf(env, canvas);
    if (window == nullptr) return 0;
    ::AcquireSRWLockExclusive(&g_lock);
    if (Find(window) == nullptr) {
        g_overlays.push_back(Overlay{window, nullptr, nullptr, kGlBack, true, false, false, 0, 0, {}});
    }
    ::ReleaseSRWLockExclusive(&g_lock);
    return reinterpret_cast<jlong>(window);
}

// Stops treating |window| as an overlay layer, once its canvas has another window or none.
JNIEXPORT void JNICALL Java_dev_equo_swt_awt_GlFrontBuffer_Untrack(JNIEnv* env, jclass cls, jlong window) {
    ::AcquireSRWLockExclusive(&g_lock);
    for (auto it = g_overlays.begin(); it != g_overlays.end(); ++it) {
        if (it->window == reinterpret_cast<HWND>(window)) {
            g_overlays.erase(it);
            break;
        }
    }
    ::ReleaseSRWLockExclusive(&g_lock);
}

// Until EndPaint, a render on this thread shows where |window|'s canvas renders its scene.
JNIEXPORT void JNICALL Java_dev_equo_swt_awt_GlFrontBuffer_BeginPaint(JNIEnv* env, jclass cls, jlong window) {
    t_painting = reinterpret_cast<HWND>(window);
}

JNIEXPORT void JNICALL Java_dev_equo_swt_awt_GlFrontBuffer_EndPaint(JNIEnv* env, jclass cls) {
    t_painting = nullptr;
}

// [width, height, pixels...] of |window|'s overlay, 0xAARRGGBB top row first; null while it has none.
JNIEXPORT jintArray JNICALL Java_dev_equo_swt_awt_GlFrontBuffer_Overlay(JNIEnv* env, jclass cls, jlong window) {
    jintArray result = nullptr;
    ::AcquireSRWLockShared(&g_lock);
    const Overlay* overlay = Find(reinterpret_cast<HWND>(window));
    if (overlay != nullptr && !overlay->pixels.empty()) {
        const jsize count = static_cast<jsize>(overlay->pixels.size());
        result = env->NewIntArray(2 + count);
        if (result != nullptr) {
            const jint size[] = {overlay->width, overlay->height};
            env->SetIntArrayRegion(result, 0, 2, size);
            env->SetIntArrayRegion(result, 2, count, reinterpret_cast<const jint*>(overlay->pixels.data()));
        }
    }
    ::ReleaseSRWLockShared(&g_lock);
    return result;
}

}  // extern "C"
