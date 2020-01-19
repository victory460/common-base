/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.data.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.bremersee.common.model.ThreeLetterCountryCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The three letter country converter test.
 *
 * @author Christian Bremer
 */
class ThreeLetterCountryConverterTest {

  private static ThreeLetterCountryCodeReadConverter readConverter;

  private static ThreeLetterCountryCodeWriteConverter writeConverter;

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    readConverter = new ThreeLetterCountryCodeReadConverter();
    writeConverter = new ThreeLetterCountryCodeWriteConverter();
  }

  /**
   * Convert.
   */
  @Test
  void convert() {
    ThreeLetterCountryCode code = ThreeLetterCountryCode.BEL;
    String actual = writeConverter.convert(code);
    assertNotNull(actual);
    assertEquals(code.toString(), actual);
    assertEquals(code, readConverter.convert(actual));
  }
}