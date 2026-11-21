#ifndef RUNNER_ENGINE_GOT_H_
#define RUNNER_ENGINE_GOT_H_

// Points the engine library's (libflutter_linux_gtk.so) calls of |symbol| at |replacement|, and
// returns how many of its GOT entries were written; 0 when the library or the symbol is not found.
//
// libflutter_linux_gtk.so is loaded in a dlopen group of its own, so a definition in the runner
// does not interpose on the functions it imports: its GOT entries for them are rewritten instead,
// found by name in its dynamic relocations. x86_64 and aarch64; the page protection is restored.
int EngineGotPatch(const char *symbol, void *replacement);

#endif // RUNNER_ENGINE_GOT_H_
