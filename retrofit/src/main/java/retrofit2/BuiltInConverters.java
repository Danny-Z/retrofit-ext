/*
 * Copyright (C) 2015 Square, Inc.
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
package retrofit2;

import kotlin.Unit;
import okhttp3.RequestBody;
import retrofit2.http.Streaming;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

final class BuiltInConverters extends Converter.Factory {
    /**
     * Not volatile because we don't mind multiple threads discovering this.
     */
    private boolean checkForKotlinUnit = true;

    @Override
    public Converter<okhttp3.Response, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                                Retrofit retrofit) {
        if (type == okhttp3.Response.class) {
            return Utils.isAnnotationPresent(annotations, Streaming.class)
                    ? StreamingResponseBodyConverter.INSTANCE
                    : BufferingResponseBodyConverter.INSTANCE;
        }
        if (type == Void.class) {
            return VoidResponseBodyConverter.INSTANCE;
        }
        if (checkForKotlinUnit) {
            try {
                if (type == Unit.class) {
                    return UnitResponseBodyConverter.INSTANCE;
                }
            } catch (NoClassDefFoundError ignored) {
                checkForKotlinUnit = false;
            }
        }
        return null;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (RequestBody.class.isAssignableFrom(Utils.getRawType(type))) {
            return RequestBodyConverter.INSTANCE;
        }
        return null;
    }

    static final class VoidResponseBodyConverter implements Converter<okhttp3.Response, Void> {
        static final VoidResponseBodyConverter INSTANCE = new VoidResponseBodyConverter();

        @Override
        public Void convert(okhttp3.Response value) {
            value.close();
            return null;
        }
    }

    static final class UnitResponseBodyConverter implements Converter<okhttp3.Response, Unit> {
        static final UnitResponseBodyConverter INSTANCE = new UnitResponseBodyConverter();

        @Override
        public Unit convert(okhttp3.Response value) {
            value.close();
            return Unit.INSTANCE;
        }
    }

    static final class RequestBodyConverter implements Converter<RequestBody, RequestBody> {
        static final RequestBodyConverter INSTANCE = new RequestBodyConverter();

        @Override
        public RequestBody convert(RequestBody value) {
            return value;
        }
    }

    static final class StreamingResponseBodyConverter
            implements Converter<okhttp3.Response, okhttp3.Response> {
        static final StreamingResponseBodyConverter INSTANCE = new StreamingResponseBodyConverter();

        @Override
        public okhttp3.Response convert(okhttp3.Response value) {
            return value;
        }
    }

    static final class BufferingResponseBodyConverter
            implements Converter<okhttp3.Response, okhttp3.Response> {
        static final BufferingResponseBodyConverter INSTANCE = new BufferingResponseBodyConverter();

        @Override
        public okhttp3.Response convert(okhttp3.Response value) throws IOException {
            return value;
        }
    }

    static final class ToStringConverter implements Converter<Object, String> {
        static final ToStringConverter INSTANCE = new ToStringConverter();

        @Override
        public String convert(Object value) {
            return value.toString();
        }
    }
}
