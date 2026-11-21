#ifndef OPAQUE_PRESENT_H_
#define OPAQUE_PRESENT_H_

// Hands GDK each Flutter frame as an opaque texture, for a window Flutter paints whole. Process-wide
// and idempotent. Returns whether frames are presented that way; when false, the engine presents as
// it always has. X11 only. See opaque_present.cc.
bool OpaquePresentInstall();

#endif // OPAQUE_PRESENT_H_
