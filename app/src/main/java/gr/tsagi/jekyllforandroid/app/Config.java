/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.tsagi.jekyllforandroid.app;

public class Config {
    // General configuration

    // Is this an internal dogfood build?
    public static final boolean IS_DOGFOOD_BUILD = true;

    // Warning messages for dogfood build
    public static final String DOGFOOD_BUILD_WARNING_TITLE = "Test build";
    public static final String DOGFOOD_BUILD_WARNING_TEXT = "This is a test build.";

    // OAuth 2.0 related config
    public static final String APP_NAME = "Jekyll For Android";
    public static final String API_KEY = "";

    // Values for the EventPoint feedback API. Sync happens at the same time as schedule sync,
    // and before that values are stored locally in the database.

    public static final String FEEDBACK_API_CODE = "";
    public static final String FEEDBACK_URL = "";
    public static final String FEEDBACK_API_KEY = "";

    private static String piece(String s, char start, char end) {
        int startIndex = s.indexOf(start), endIndex = s.indexOf(end);
        return s.substring(startIndex + 1, endIndex);
    }

    private static String piece(String s, char start) {
        int startIndex = s.indexOf(start);
        return s.substring(startIndex + 1);
    }

    private static String rep(String s, String orig, String replacement) {
        return s.replaceAll(orig, replacement);
    }
}
