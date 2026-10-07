# Third-party notices

These component notices do not license the project's own code or artwork. See [LICENSE-STATUS.md](LICENSE-STATUS.md).

| Component | Role | License / official reference |
| --- | --- | --- |
| Gradle wrapper scripts and JAR, 8.13 | Included build bootstrap files, retained from source | Apache License 2.0; [license text](licenses/Apache-2.0.txt), [Gradle source](https://github.com/gradle/gradle), [checksums](https://gradle.org/release-checksums/) |
| Android Gradle Plugin 8.9.2 | Downloaded build dependency | Apache License 2.0; [Android build tools](https://android.googlesource.com/platform/tools/base/) |
| JUnit 4.13.2 | Downloaded unit-test dependency | Eclipse Public License 1.0; [JUnit license](https://junit.org/junit4/license.html) |
| Hamcrest Core 1.3 | JUnit transitive test dependency | BSD license; [Hamcrest license](https://hamcrest.org/JavaHamcrest/distributables) |
| Android SDK platform / Build Tools 35 | External development prerequisite, not bundled | [Android SDK agreement](https://developer.android.com/studio/terms) and component notices |

The application declares no runtime third-party libraries. Build tools have transitive dependencies; this table identifies the direct build/test inputs and JUnit's expected assertion dependency, not a complete resolved software bill of materials. Review the resolved dependency/license report before release.

Gradle wrapper JAR SHA-256: `81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f`.
Gradle 8.13 binary ZIP SHA-256: `20f1b1176237254a6fc204d8434196fa11a4cfb387567519c61556e8710aed78`.
Both match the official Gradle checksum page checked during preparation.

No official OpenAI character images, logos, third-party pet atlases, stock photographs, or external fonts are bundled. The generic paw vector icon is retained from the supplied source; no separate original attribution/license was present. The owner should confirm its provenance before public licensing. New raster artwork has its own [provenance record](docs/ASSET_PROVENANCE.md).

## 日本語

Gradle wrapperの既存表記は維持しています。コード・素材全体のライセンスを勝手に決めるものではありません。実行時の外部ライブラリはありませんが、ビルドツールの推移的依存まですべてを列挙したSBOMではないため、公開前に解決済み依存を再確認してください。元ソースの汎用肉球アイコンは出所の追加記録がないため、所有者側の確認事項に残しています。
