/*
 * Copyright (C) 2023 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wiremock.grpc.dsl;

import com.github.tomakehurst.wiremock.common.Exceptions;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

public enum GrpcStatus {
  OK(0);

  private static final JsonFormat.Printer jsonPrinter = JsonFormat.printer();

  private final int value;

  GrpcStatus(int value) {
    this.value = value;
  }

  public GrpcResponseDefinitionBuilder json(String json) {
    return new GrpcResponseDefinitionBuilder(value).fromJson(json);
  }

  public GrpcResponseDefinitionBuilder message(MessageOrBuilder messageOrBuilder) {
    final String json = Exceptions.uncheck(() -> jsonPrinter.print(messageOrBuilder), String.class);
    return new GrpcResponseDefinitionBuilder(value).fromJson(json);
  }
}
