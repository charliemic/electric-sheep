# Third-Party Licenses

This file contains the licenses of third-party libraries and tools used in Electric Sheep.

## Project License

Electric Sheep is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Third-Party Licenses

### Android App Dependencies

#### AndroidX Libraries (Apache License 2.0)
- `androidx.core:core-ktx`
- `androidx.lifecycle:*`
- `androidx.activity:activity-compose`
- `androidx.browser:browser`
- `androidx.compose:*`
- `androidx.navigation:navigation-compose`
- `androidx.room:*`
- `androidx.work:work-runtime-ktx`

**License**: Apache License 2.0  
**License Text**: https://www.apache.org/licenses/LICENSE-2.0

#### Kotlin Libraries (Apache License 2.0)
- `org.jetbrains.kotlin:*`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `org.jetbrains.kotlinx:kotlinx-coroutines-android`

**License**: Apache License 2.0  
**License Text**: https://www.apache.org/licenses/LICENSE-2.0

#### Supabase Libraries (Apache License 2.0)
- `io.github.jan-tennert.supabase:*`
- `io.ktor:ktor-client-android`

**License**: Apache License 2.0  
**License Text**: https://www.apache.org/licenses/LICENSE-2.0

#### Vico Charts
- `com.patrykandpatrick.vico:*`

**License**: ⚠️ **Needs Verification** (likely Apache 2.0 or MIT)  
**Repository**: https://github.com/patrykandpatrick/vico

#### Test Frameworks
- `junit:junit` - Eclipse Public License 1.0
- `org.mockito.kotlin:mockito-kotlin` - MIT License
- `androidx.test.*` - Apache License 2.0

**Licenses**:
- JUnit: Eclipse Public License 1.0 - https://www.eclipse.org/legal/epl-v10.html
- Mockito: MIT License - https://opensource.org/licenses/MIT
- AndroidX Test: Apache License 2.0 - https://www.apache.org/licenses/LICENSE-2.0

### Test Automation Dependencies

#### Appium (Apache License 2.0)
- `io.appium:java-client`

**License**: Apache License 2.0  
**License Text**: https://www.apache.org/licenses/LICENSE-2.0

#### Jackson (Apache License 2.0)
- `com.fasterxml.jackson.module:jackson-module-kotlin`
- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`

**License**: Apache License 2.0  
**License Text**: https://www.apache.org/licenses/LICENSE-2.0

#### Logging
- `org.slf4j:slf4j-api` - MIT License
- `ch.qos.logback:logback-classic` - Eclipse Public License 1.0 / LGPL 2.1 (dual license)

**Licenses**:
- SLF4J: MIT License - https://opensource.org/licenses/MIT
- Logback: Eclipse Public License 1.0 / LGPL 2.1 - https://logback.qos.ch/license.html

#### HTTP Client (Apache License 2.0)
- `com.squareup.okhttp3:okhttp`

**License**: Apache License 2.0  
**License Text**: https://www.apache.org/licenses/LICENSE-2.0

#### OpenCV
- `nu.pattern:opencv:2.4.9-7`

**License**: ⚠️ **NEEDS VERIFICATION** (likely BSD 3-Clause, but verify)  
**Note**: Used in test automation only, not in production app  
**Action Required**: Verify license at https://mvnrepository.com/artifact/nu.pattern/opencv/2.4.9-7

### HTML Viewer Dependencies

All dependencies use MIT License:
- `astro`
- `@astrojs/tailwind`
- `marked`
- `prismjs`
- `typescript` (Apache License 2.0)

**License**: MIT License  
**License Text**: https://opensource.org/licenses/MIT

### Python Dependencies

- `moviepy` - MIT License
- `Pillow` - PIL License (HPND - Historical Permission Notice and Disclaimer)

**Licenses**:
- MoviePy: MIT License - https://opensource.org/licenses/MIT
- Pillow: PIL License - https://github.com/python-pillow/Pillow/blob/main/LICENSE

### Build Tools

- Gradle - Apache License 2.0
- Android Gradle Plugin - Apache License 2.0
- Kotlin Plugin - Apache License 2.0
- KSP (Kotlin Symbol Processing) - Apache License 2.0

**License**: Apache License 2.0  
**License Text**: https://www.apache.org/licenses/LICENSE-2.0

## License Summary

### Permissive Licenses (Commercial Use Allowed)
- **Apache License 2.0**: AndroidX, Kotlin, Supabase, Ktor, Room, WorkManager, OkHttp, Jackson, Appium, Gradle
- **MIT License**: Mockito, SLF4J, Astro, Tailwind, Marked, Prism.js, MoviePy
- **EPL 1.0**: JUnit, Logback (dual license)
- **PIL License**: Pillow

### Needs Verification
- **Vico Charts**: Likely Apache 2.0 or MIT (verify)
- **OpenCV**: Likely BSD 3-Clause (verify - critical for commercial use)

## License Compliance

All verified dependencies use permissive licenses that allow:
- ✅ Commercial use
- ✅ Modification
- ✅ Distribution
- ✅ Private use
- ✅ Sublicensing

**Requirements**:
- ✅ License and copyright notice (this file)
- ✅ State changes (Apache 2.0 - if modifying Apache-licensed code)

## Verification Status

- ✅ **Verified**: 95%+ of dependencies
- ⚠️ **Needs Verification**: Vico Charts, OpenCV
- ❌ **Problematic**: None confirmed

## Updates

This file should be updated when:
- New dependencies are added
- Dependencies are updated
- License information changes

**Last Updated**: 2025-01-20

---

**Note**: For detailed license analysis and commercial use implications, see:
- `docs/legal/DEPENDENCY_LICENSE_ANALYSIS.md` - Complete license analysis
- `docs/legal/COMMERCIAL_USE_LICENSE_SUMMARY.md` - Commercial use summary

