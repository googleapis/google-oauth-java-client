# Changelog

## [1.37.0](https://github.com/googleapis/google-oauth-java-client/compare/v1.36.0...v1.37.0) (2024-10-30)


### Documentation

* Update javadoc sample code comments to render correctly in Cloud RAD ([#1121](https://github.com/googleapis/google-oauth-java-client/issues/1121)) ([ab4d5e7](https://github.com/googleapis/google-oauth-java-client/commit/ab4d5e7dae926b5d95ddc3021a82883a0f24415d))

## [1.36.0](https://github.com/googleapis/google-oauth-java-client/compare/v1.35.0...v1.36.0) (2024-05-10)


### Features

* Servlet classes that use the jakarta namespace ([#1115](https://github.com/googleapis/google-oauth-java-client/issues/1115)) ([11d6a3c](https://github.com/googleapis/google-oauth-java-client/commit/11d6a3cb30c4ebfe4fc4e196d99f5764c6ade878))

## [1.35.0](https://github.com/googleapis/google-oauth-java-client/compare/v1.34.1...v1.35.0) (2023-12-29)


### Features

* Add googleapis-auth as codeowner team ([#923](https://github.com/googleapis/google-oauth-java-client/issues/923)) ([d7dc38c](https://github.com/googleapis/google-oauth-java-client/commit/d7dc38c3a701b074ac90faf6618f594c3675d126))


### Bug Fixes

* Adding retries to public key fetch for IdTokenVerifier to cover transient network issues. ([289f139](https://github.com/googleapis/google-oauth-java-client/commit/289f1397168ac825da68907b050a1ef41827c4ca))
* Deprecating the IdTokenVerifier.verify, adding verifyOrThrow as an alternative ([#1091](https://github.com/googleapis/google-oauth-java-client/issues/1091)) ([a9a062e](https://github.com/googleapis/google-oauth-java-client/commit/a9a062ef8665495055571534b9fff4fcc630c8ad))
* **test:** Update test tokens ([#953](https://github.com/googleapis/google-oauth-java-client/issues/953)) ([d523410](https://github.com/googleapis/google-oauth-java-client/commit/d5234104b28dfcc3c9424e200f2ede8832bede6f))
* Update test tokens ([#971](https://github.com/googleapis/google-oauth-java-client/issues/971)) ([c28d149](https://github.com/googleapis/google-oauth-java-client/commit/c28d1495a9095753544651294373990207b25203))


### Dependencies

* Update doclet version to 1.9.0 ([#1054](https://github.com/googleapis/google-oauth-java-client/issues/1054)) ([ca216f9](https://github.com/googleapis/google-oauth-java-client/commit/ca216f9fd1499a2d76a199c74adeff5fd0d8b122))

## [1.34.1](https://github.com/googleapis/google-oauth-java-client/compare/v1.34.0...v1.34.1) (2022-06-09)


### Bug Fixes

* .repo-metadata.json file with https: [#813](https://github.com/googleapis/google-oauth-java-client/issues/813) ([#898](https://github.com/googleapis/google-oauth-java-client/issues/898)) ([be4d54a](https://github.com/googleapis/google-oauth-java-client/commit/be4d54ad6b3264a1246cd1bd5789140112407681))


### Dependencies

* update project.http.version to v1.42.0 ([#902](https://github.com/googleapis/google-oauth-java-client/issues/902)) ([51c1eb5](https://github.com/googleapis/google-oauth-java-client/commit/51c1eb5e4c6797b51b4347046422681780bd0d81))

## [1.34.0](https://github.com/googleapis/google-oauth-java-client/compare/v1.33.3...v1.34.0) (2022-06-02)


### Features

* add build scripts for native image testing in Java 17 ([#1440](https://github.com/googleapis/google-oauth-java-client/issues/1440)) ([#890](https://github.com/googleapis/google-oauth-java-client/issues/890)) ([373891e](https://github.com/googleapis/google-oauth-java-client/commit/373891e2dc9742fdf8954cc590b18caf4c8c44f7))
* next release from main branch is 1.34.0 ([#875](https://github.com/googleapis/google-oauth-java-client/issues/875)) ([187651e](https://github.com/googleapis/google-oauth-java-client/commit/187651eeb963c490c1a5595222548bbdba660c22))


### Bug Fixes

* fix IdTokenVerifier so it does not cache empty entries ([#892](https://github.com/googleapis/google-oauth-java-client/issues/892)) ([773b388](https://github.com/googleapis/google-oauth-java-client/commit/773b38844cd6a0a72a360cc25692412e9b36b1e7))

### [1.33.3](https://github.com/googleapis/google-oauth-java-client/compare/v1.33.2...v1.33.3) (2022-04-13)


### Bug Fixes

* add signature verification to IdTokenVerifier ([#861](https://github.com/googleapis/google-oauth-java-client/issues/861)) ([22419d6](https://github.com/googleapis/google-oauth-java-client/commit/22419d60579ef4c1a8a256a90e6ca7bc58f09aa1))

### [1.33.2](https://github.com/googleapis/google-oauth-java-client/compare/v1.33.1...v1.33.2) (2022-04-07)


### Dependencies

* update project.http.version to v1.41.5 ([e945b8d](https://github.com/googleapis/google-oauth-java-client/commit/e945b8d7233038f417f40771508d171f6f0cbaf5))

### [1.33.1](https://github.com/googleapis/google-oauth-java-client/compare/v1.33.0...v1.33.1) (2022-02-08)


### Dependencies

* **java:** update actions/github-script action to v5 ([#1339](https://github.com/googleapis/google-oauth-java-client/issues/1339)) ([#822](https://github.com/googleapis/google-oauth-java-client/issues/822)) ([1f15374](https://github.com/googleapis/google-oauth-java-client/commit/1f15374fe935bf46e9cda59270694f682ba5f75b))

## [1.33.0](https://github.com/googleapis/google-oauth-java-client/compare/v1.32.1...v1.33.0) (2022-01-19)


### Features

* next release from main branch is 1.33.0 ([#772](https://github.com/googleapis/google-oauth-java-client/issues/772)) ([4c3a639](https://github.com/googleapis/google-oauth-java-client/commit/4c3a6399f6d4aa4871bd119de378965e187e58b3))


### Bug Fixes

* **java:** add -ntp flag to native image testing command ([#1299](https://github.com/googleapis/google-oauth-java-client/issues/1299)) ([#784](https://github.com/googleapis/google-oauth-java-client/issues/784)) ([5ab7e71](https://github.com/googleapis/google-oauth-java-client/commit/5ab7e71390fbb2077b990e7a4b846b0eaa91d8a5))
* **java:** java 17 dependency arguments ([#1266](https://github.com/googleapis/google-oauth-java-client/issues/1266)) ([#764](https://github.com/googleapis/google-oauth-java-client/issues/764)) ([34318c5](https://github.com/googleapis/google-oauth-java-client/commit/34318c5342dbf6226c9959dcef26e45ecbcb650f))
* **java:** run Maven in plain console-friendly mode ([#1301](https://github.com/googleapis/google-oauth-java-client/issues/1301)) ([#790](https://github.com/googleapis/google-oauth-java-client/issues/790)) ([894bbfc](https://github.com/googleapis/google-oauth-java-client/commit/894bbfc751099d19b5f18fe70c2e068b1f6fd09c))
* new java format dependencies and linter fix ([#768](https://github.com/googleapis/google-oauth-java-client/issues/768)) ([9df1cd7](https://github.com/googleapis/google-oauth-java-client/commit/9df1cd70057bbb4f37f487f10d309d651ed68d20))

### [1.32.1](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.32.0...v1.32.1) (2021-08-12)


### Features

* add `gcf-owl-bot[bot]` to `ignoreAuthors` ([#690](https://www.github.com/googleapis/google-oauth-java-client/issues/690)) ([2786401](https://www.github.com/googleapis/google-oauth-java-client/commit/27864015793eab524c83ba4fc345a1afc27d7a95))
* Add HMAC-SHA256 signature method for OAuth 1.0 ([#711](https://www.github.com/googleapis/google-oauth-java-client/issues/711)) ([c070f5f](https://www.github.com/googleapis/google-oauth-java-client/commit/c070f5f27d8034f681b7fc9a43825cfc7fd6f06f))


### Bug Fixes

* release scripts from issuing overlapping phases ([#664](https://www.github.com/googleapis/google-oauth-java-client/issues/664)) ([60fec2b](https://www.github.com/googleapis/google-oauth-java-client/commit/60fec2b9bbd5d632dff155a45a2ed0fa2f261c45))
* Revert "chore(deps): update dependency com.google.googlejavaformat:google-java-format to v1.10.0" to fix linter ([#713](https://www.github.com/googleapis/google-oauth-java-client/issues/713)) ([bbc9ea2](https://www.github.com/googleapis/google-oauth-java-client/commit/bbc9ea2865f30a12402869e427bc6ae3ebffc588))
* Update dependencies.sh to not break on mac ([#706](https://www.github.com/googleapis/google-oauth-java-client/issues/706)) ([39c2777](https://www.github.com/googleapis/google-oauth-java-client/commit/39c2777543ca46dc2e4c12bd5469e829c9a85c37))

## [1.32.0](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.31.5...v1.32.0) (2021-08-11)


### Features

* add `gcf-owl-bot[bot]` to `ignoreAuthors` ([#690](https://www.github.com/googleapis/google-oauth-java-client/issues/690)) ([2786401](https://www.github.com/googleapis/google-oauth-java-client/commit/27864015793eab524c83ba4fc345a1afc27d7a95))
* Add HMAC-SHA256 signature method for OAuth 1.0 ([#711](https://www.github.com/googleapis/google-oauth-java-client/issues/711)) ([c070f5f](https://www.github.com/googleapis/google-oauth-java-client/commit/c070f5f27d8034f681b7fc9a43825cfc7fd6f06f))


### Bug Fixes

* release scripts from issuing overlapping phases ([#664](https://www.github.com/googleapis/google-oauth-java-client/issues/664)) ([60fec2b](https://www.github.com/googleapis/google-oauth-java-client/commit/60fec2b9bbd5d632dff155a45a2ed0fa2f261c45))
* Revert "chore(deps): update dependency com.google.googlejavaformat:google-java-format to v1.10.0" to fix linter ([#713](https://www.github.com/googleapis/google-oauth-java-client/issues/713)) ([bbc9ea2](https://www.github.com/googleapis/google-oauth-java-client/commit/bbc9ea2865f30a12402869e427bc6ae3ebffc588))
* Update dependencies.sh to not break on mac ([#706](https://www.github.com/googleapis/google-oauth-java-client/issues/706)) ([39c2777](https://www.github.com/googleapis/google-oauth-java-client/commit/39c2777543ca46dc2e4c12bd5469e829c9a85c37))

### [1.31.5](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.31.4...v1.31.5) (2021-04-09)


### Bug Fixes

* don't swallow exceptions in LocalServerReceiver ([#595](https://www.github.com/googleapis/google-oauth-java-client/issues/595)) ([f39faec](https://www.github.com/googleapis/google-oauth-java-client/commit/f39faec9980fa65602a216fbf34555b744139443))
* oauth1 signing for url encoded content ([#538](https://www.github.com/googleapis/google-oauth-java-client/issues/538)) ([d9507e4](https://www.github.com/googleapis/google-oauth-java-client/commit/d9507e4c367cc870b811e28e3b206ef4661c67d8))
* remove Jackson from assembly ([#605](https://www.github.com/googleapis/google-oauth-java-client/issues/605)) ([a482000](https://www.github.com/googleapis/google-oauth-java-client/commit/a482000eddf3c056f57492487c4a2f1e2f81feeb))
* switch to GSON per security team advice ([#586](https://www.github.com/googleapis/google-oauth-java-client/issues/586)) ([58a1828](https://www.github.com/googleapis/google-oauth-java-client/commit/58a1828e8e291c59494893b2632c294dffe98b23))


### Dependencies

* update appengine packages to v1.9.84 ([#577](https://www.github.com/googleapis/google-oauth-java-client/issues/577)) ([3fbd4d5](https://www.github.com/googleapis/google-oauth-java-client/commit/3fbd4d5205215447969adb7fa93a46f309eed4a5))

### [1.31.4](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.31.3...v1.31.4) (2021-01-05)


### Dependencies

* update dependency com.google.guava:guava to v30.1-android ([#578](https://www.github.com/googleapis/google-oauth-java-client/issues/578)) ([a719fbb](https://www.github.com/googleapis/google-oauth-java-client/commit/a719fbb03701938aac125f456153433e41b69393))

### [1.31.3](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.31.2...v1.31.3) (2020-12-01)


### Dependencies

* fix declared dependencies ([#570](https://www.github.com/googleapis/google-oauth-java-client/issues/570)) ([ec79525](https://www.github.com/googleapis/google-oauth-java-client/commit/ec79525da8bc50d4cb641a87c186a5870a61afd4))

### [1.31.2](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.31.1...v1.31.2) (2020-11-04)


### Dependencies

* update appengine packages to v1.9.83 ([#557](https://www.github.com/googleapis/google-oauth-java-client/issues/557)) ([7d89fd3](https://www.github.com/googleapis/google-oauth-java-client/commit/7d89fd36810e5c29073e0ab571e7e433d4473996))
* update dependency com.google.guava:guava to v30 ([#549](https://www.github.com/googleapis/google-oauth-java-client/issues/549)) ([e3a0903](https://www.github.com/googleapis/google-oauth-java-client/commit/e3a0903fc405ea6fa86e62032dfb2a9fc9a23d1f))
* update google-http-client to v1.38.0 ([#556](https://www.github.com/googleapis/google-oauth-java-client/issues/556)) ([71840b4](https://www.github.com/googleapis/google-oauth-java-client/commit/71840b44348f70f9c1e226f51aae3761d71dc341))

### [1.31.1](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.31.0...v1.31.1) (2020-10-13)


### Dependencies

* update appengine packages to v1.9.82 ([#465](https://www.github.com/googleapis/google-oauth-java-client/issues/465)) ([651256c](https://www.github.com/googleapis/google-oauth-java-client/commit/651256caaaa0f760c6e098d6dae10b0c939564d4))
* update google-http-client to v1.37.0 ([#544](https://www.github.com/googleapis/google-oauth-java-client/issues/544)) ([26a1e6d](https://www.github.com/googleapis/google-oauth-java-client/commit/26a1e6d17f984cc6c3d5a9d7dbfe984eda2c27bd))

## [1.31.0](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.30.6...v1.31.0) (2020-06-29)


### Features

* add PKCE support to AuthorizationCodeFlow ([#470](https://www.github.com/googleapis/google-oauth-java-client/issues/470)) ([13433cd](https://www.github.com/googleapis/google-oauth-java-client/commit/13433cd7dd06267fc261f0b1d4764f8e3432c824))


### Dependencies

* update google-http-client to v1.35.0 ([#466](https://www.github.com/googleapis/google-oauth-java-client/issues/466)) ([6447917](https://www.github.com/googleapis/google-oauth-java-client/commit/6447917c657a5ae4267afbab74dfdb890bbfbf28))
* update to guava 29.0-android ([#456](https://www.github.com/googleapis/google-oauth-java-client/issues/456)) ([fc75233](https://www.github.com/googleapis/google-oauth-java-client/commit/fc752336af9cbdb9b2ed816a63d7bd3d8d1e2778))

### [1.30.6](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.30.5...v1.30.6) (2020-02-24)


### Bug Fixes

* remove vestigial, outdated protobuf dependencies from assembly docs ([#409](https://www.github.com/googleapis/google-oauth-java-client/issues/409)) ([bc8a5aa](https://www.github.com/googleapis/google-oauth-java-client/commit/bc8a5aa3745b414bea035d9dad66882be7ad6311))


### Dependencies

* replace Jetty with HttpServer ([#433](https://www.github.com/googleapis/google-oauth-java-client/issues/433)) ([bcabce2](https://www.github.com/googleapis/google-oauth-java-client/commit/bcabce25df8b7dc9d3d0edfca009d47a465d1af3)), closes [#397](https://www.github.com/googleapis/google-oauth-java-client/issues/397)
* update dependency commons-codec:commons-codec to v1.14 ([#412](https://www.github.com/googleapis/google-oauth-java-client/issues/412)) ([f5abf5e](https://www.github.com/googleapis/google-oauth-java-client/commit/f5abf5e00b9785f48fdea55a9d993565c66af61a))

### [1.30.5](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.30.4...v1.30.5) (2019-12-17)


### Dependencies

* update project.http.version to v1.34.0 ([#405](https://www.github.com/googleapis/google-oauth-java-client/issues/405)) ([61c21c7](https://www.github.com/googleapis/google-oauth-java-client/commit/61c21c7f6be6aca8285e3fedf1edab9a0faf3570))

### [1.30.4](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.30.3...v1.30.4) (2019-10-22)


### Documentation

* fix navigation link to OAuth 2.0 page ([09dba36](https://www.github.com/googleapis/google-oauth-java-client/commit/09dba36c4166fd1a062cc75e8688cd933c30f21d))
* move wiki to docs area ([#386](https://www.github.com/googleapis/google-oauth-java-client/issues/386)) ([ee7e98b](https://www.github.com/googleapis/google-oauth-java-client/commit/ee7e98b187251f1031863ad02790bf37a65b5691))


### Dependencies

* update dependency mysql:mysql-connector-java to v8.0.18 ([#381](https://www.github.com/googleapis/google-oauth-java-client/issues/381)) ([3e351de](https://www.github.com/googleapis/google-oauth-java-client/commit/3e351de3fb9a70a9174d06970b60d2387955e196))
* update jetty to 8.2 ([#377](https://www.github.com/googleapis/google-oauth-java-client/issues/377)) ([6584664](https://www.github.com/googleapis/google-oauth-java-client/commit/658466473c4f016c356e3647234e19c9166fcaec))

### [1.30.3](https://www.github.com/googleapis/google-oauth-java-client/compare/v1.30.2...v1.30.3) (2019-09-20)


### Dependencies

* update google-http-client to v1.32.1 ([#372](https://www.github.com/googleapis/google-oauth-java-client/issues/372)) ([cfaee50](https://www.github.com/googleapis/google-oauth-java-client/commit/cfaee50))
* update guava to 28.1-android ([#374](https://www.github.com/googleapis/google-oauth-java-client/issues/374)) ([d86a67a](https://www.github.com/googleapis/google-oauth-java-client/commit/d86a67a))
