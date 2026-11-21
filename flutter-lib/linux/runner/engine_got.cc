#include "engine_got.h"

#include <elf.h>
#include <link.h>
#include <sys/mman.h>
#include <unistd.h>

#include <cstdint>
#include <cstdio>
#include <cstring>
#include <utility>

namespace {

#if defined(__x86_64__)
constexpr uint32_t kJumpSlot = R_X86_64_JUMP_SLOT;
constexpr uint32_t kGlobDat = R_X86_64_GLOB_DAT;
#elif defined(__aarch64__)
constexpr uint32_t kJumpSlot = R_AARCH64_JUMP_SLOT;
constexpr uint32_t kGlobDat = R_AARCH64_GLOB_DAT;
#endif

struct Library {
  const char *suffix;
  const dl_phdr_info *info = nullptr;
  dl_phdr_info copy;
};

int FindLibrary(dl_phdr_info *info, size_t, void *data) {
  auto *lib = static_cast<Library *>(data);
  size_t n = info->dlpi_name ? strlen(info->dlpi_name) : 0, s = strlen(lib->suffix);
  if (n >= s && strcmp(info->dlpi_name + n - s, lib->suffix) == 0) {
    lib->copy = *info;
    lib->info = &lib->copy;
    return 1;
  }
  return 0;
}

// The protection of the page holding addr, from /proc/self/maps; 0 when it cannot be told.
int PageProtection(uintptr_t addr) {
  FILE *maps = fopen("/proc/self/maps", "r");
  if (maps == nullptr) return 0;
  char line[512];
  int prot = 0;
  while (fgets(line, sizeof line, maps)) {
    unsigned long lo = 0, hi = 0;
    char perms[5] = {0};
    if (sscanf(line, "%lx-%lx %4s", &lo, &hi, perms) == 3 && addr >= lo && addr < hi) {
      prot = (perms[0] == 'r' ? PROT_READ : 0) | (perms[1] == 'w' ? PROT_WRITE : 0) | (perms[2] == 'x' ? PROT_EXEC : 0);
      break;
    }
  }
  fclose(maps);
  return prot;
}

int PatchGot(const dl_phdr_info *lib, const char *symbol, void *replacement) {
#if defined(__x86_64__) || defined(__aarch64__)
  const ElfW(Dyn) *dynamic = nullptr;
  for (int i = 0; i < lib->dlpi_phnum; i++) {
    if (lib->dlpi_phdr[i].p_type == PT_DYNAMIC) {
      dynamic = reinterpret_cast<const ElfW(Dyn) *>(lib->dlpi_addr + lib->dlpi_phdr[i].p_vaddr);
    }
  }
  if (dynamic == nullptr) return 0;
  uintptr_t base = lib->dlpi_addr, symtab = 0, strtab = 0, jmprel = 0, rela = 0;
  size_t jmprel_size = 0, rela_size = 0;
  for (const ElfW(Dyn) *d = dynamic; d->d_tag != DT_NULL; d++) {
    switch (d->d_tag) {
    case DT_SYMTAB: symtab = d->d_un.d_ptr; break;
    case DT_STRTAB: strtab = d->d_un.d_ptr; break;
    case DT_JMPREL: jmprel = d->d_un.d_ptr; break;
    case DT_PLTRELSZ: jmprel_size = d->d_un.d_val; break;
    case DT_RELA: rela = d->d_un.d_ptr; break;
    case DT_RELASZ: rela_size = d->d_un.d_val; break;
    }
  }
  // The dynamic linker relocates these in place on most targets; an unrelocated one is an offset.
  auto at = [base](uintptr_t p) { return p != 0 && p < base ? p + base : p; };
  symtab = at(symtab), strtab = at(strtab), jmprel = at(jmprel), rela = at(rela);
  if (symtab == 0 || strtab == 0) return 0;
  const auto *syms = reinterpret_cast<const ElfW(Sym) *>(symtab);
  const auto *names = reinterpret_cast<const char *>(strtab);
  long page = sysconf(_SC_PAGESIZE);
  int written = 0;
  for (auto [table, size] : {std::pair{jmprel, jmprel_size}, std::pair{rela, rela_size}}) {
    if (table == 0) continue;
    const auto *relocs = reinterpret_cast<const ElfW(Rela) *>(table);
    for (size_t i = 0; i < size / sizeof(ElfW(Rela)); i++) {
      uint32_t type = ELF64_R_TYPE(relocs[i].r_info);
      if (type != kJumpSlot && type != kGlobDat) continue;
      if (strcmp(names + syms[ELF64_R_SYM(relocs[i].r_info)].st_name, symbol) != 0) continue;
      auto slot = reinterpret_cast<void **>(base + relocs[i].r_offset);
      uintptr_t start = reinterpret_cast<uintptr_t>(slot) & ~static_cast<uintptr_t>(page - 1);
      int prot = PageProtection(reinterpret_cast<uintptr_t>(slot));
      if (prot == 0) continue;
      if (!(prot & PROT_WRITE) && mprotect(reinterpret_cast<void *>(start), page, prot | PROT_WRITE) != 0) continue;
      __atomic_store_n(slot, replacement, __ATOMIC_RELEASE);
      if (!(prot & PROT_WRITE)) mprotect(reinterpret_cast<void *>(start), page, prot);
      written++;
    }
  }
  return written;
#else
  return 0;
#endif
}

} // namespace

int EngineGotPatch(const char *symbol, void *replacement) {
  Library lib{"/libflutter_linux_gtk.so"};
  dl_iterate_phdr(FindLibrary, &lib);
  if (lib.info == nullptr) return 0;
  return PatchGot(lib.info, symbol, replacement);
}
