// The calls of the webview_all_linux plugin that the webkit2gtk-4.0 stack of older distributions
// (RHEL 8: libsoup 2.62, glib 2.56) lacks: libsoup 3 API, implemented on libsoup 2.4, and GUri
// (glib 2.66), implemented on SoupURI. Force-included into every source of the webkit40 plugin
// build (see CMakeLists.txt next to this file).
#ifndef EVOLVE_WEBKIT40_COMPAT_H_
#define EVOLVE_WEBKIT40_COMPAT_H_

#include <gio/gio.h>
#include <libsoup/soup.h>

#if !SOUP_CHECK_VERSION(2, 99, 0)

static inline SoupStatus soup_message_get_status(SoupMessage *msg) {
  return static_cast<SoupStatus>(msg->status_code);
}

static inline const char *soup_message_get_method(SoupMessage *msg) {
  return msg->method;
}

static inline SoupMessageHeaders *
soup_message_get_request_headers(SoupMessage *msg) {
  return msg->request_headers;
}

static inline SoupMessageHeaders *
soup_message_get_response_headers(SoupMessage *msg) {
  return msg->response_headers;
}

static inline void soup_message_set_request_body_from_bytes(
    SoupMessage *msg, const char *content_type, GBytes *bytes) {
  gsize size = 0;
  const gchar *data =
      bytes != nullptr
          ? static_cast<const gchar *>(g_bytes_get_data(bytes, &size))
          : nullptr;
  soup_message_set_request(msg, content_type, SOUP_MEMORY_COPY, data, size);
}

static inline void evolve_soup2_send_and_read_cb(SoupSession *, SoupMessage *msg,
                                                 gpointer user_data) {
  GTask *task = G_TASK(user_data);
  if (SOUP_STATUS_IS_TRANSPORT_ERROR(msg->status_code)) {
    g_task_return_new_error(
        task, G_IO_ERROR, G_IO_ERROR_FAILED, "%s",
        msg->reason_phrase != nullptr ? msg->reason_phrase : "Request failed");
  } else {
    SoupBuffer *buffer = soup_message_body_flatten(msg->response_body);
    g_task_return_pointer(task, soup_buffer_get_as_bytes(buffer),
                          reinterpret_cast<GDestroyNotify>(g_bytes_unref));
    soup_buffer_free(buffer);
  }
  g_object_unref(task);
}

// libsoup 3 reports a failed request through the GError; an HTTP error status still returns the
// body. soup_session_queue_message takes over one reference to the message.
static inline void soup_session_send_and_read_async(
    SoupSession *session, SoupMessage *msg, int io_priority,
    GCancellable *cancellable, GAsyncReadyCallback callback,
    gpointer user_data) {
  GTask *task = g_task_new(session, cancellable, callback, user_data);
  g_task_set_priority(task, io_priority);
  soup_session_queue_message(session, SOUP_MESSAGE(g_object_ref(msg)),
                             evolve_soup2_send_and_read_cb, task);
}

static inline GBytes *soup_session_send_and_read_finish(SoupSession *session,
                                                        GAsyncResult *result,
                                                        GError **error) {
  g_return_val_if_fail(g_task_is_valid(result, session), nullptr);
  return static_cast<GBytes *>(g_task_propagate_pointer(G_TASK(result), error));
}

#if !GLIB_CHECK_VERSION(2, 66, 0)
typedef SoupURI GUri;
#define G_URI_FLAGS_NONE 0
#endif

#if !GLIB_CHECK_VERSION(2, 66, 0) || GLIB_VERSION_MAX_ALLOWED < GLIB_VERSION_2_66
static inline GUri *evolve_g_uri_parse(const gchar *uri_string, int, GError **) {
  return reinterpret_cast<GUri *>(soup_uri_new(uri_string));
}

static inline const gchar *evolve_g_uri_get_host(GUri *uri) {
  return soup_uri_get_host(reinterpret_cast<SoupURI *>(uri));
}

static inline void evolve_g_uri_unref(GUri *uri) {
  soup_uri_free(reinterpret_cast<SoupURI *>(uri));
}

#define g_uri_parse evolve_g_uri_parse
#define g_uri_get_host evolve_g_uri_get_host
#define g_uri_unref evolve_g_uri_unref
#endif

#endif // !SOUP_CHECK_VERSION(2, 99, 0)

#endif // EVOLVE_WEBKIT40_COMPAT_H_
