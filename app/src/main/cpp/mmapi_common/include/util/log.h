/*
 * Taho fork of J2ME Loader: logging macros for the native audio code. The upstream header carried the licence
 * text of two licences at once; these macros are plain wrappers around __android_log_print.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef MMAPI_LOG_H
#define MMAPI_LOG_H

#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif

#define ALOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define ALOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#define android_errorWriteLog(a, b) ALOGE(b)

#ifdef __cplusplus
} /* extern "C" */
#endif

#endif /* MMAPI_LOG_H */
