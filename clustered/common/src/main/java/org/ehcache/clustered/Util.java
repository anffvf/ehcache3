/*
 * Copyright Terracotta, Inc.
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
package org.ehcache.clustered;

/**
 *
 * @author cdennis
 */
public final class Util {

  private Util() {}

  public static <T extends Exception> T unwrapException(Throwable t, Class<T> aClass) {
    Throwable cause = t.getCause();
    if (cause != null) {
      t = cause;
    }

    if (aClass.isInstance(t)) {
      return aClass.cast(t);
    } else if (t instanceof RuntimeException) {
      throw (RuntimeException) t;
    } else if (t instanceof Error) {
      throw (Error) t;
    } else {
      throw new RuntimeException(t);
    }
  }
}
