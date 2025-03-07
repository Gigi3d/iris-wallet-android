# Iris Wallet

Iris Wallet manages RGB assets from issuance to spending and receiving,
wrapping all functionality in a familiar-looking wallet application and
abstracting away as many technical details as possible.

The RGB functionality is provided by [rgb-lib] via [rgb-lib-kotlin], while the
Bitcoin functionality is provided by [bdk-kotlin].


## Variants

The project has Signet and Testnet variants.

Together with the Debug and Release targets, this produces 4 possible versions
of the app:
- bitcoinSignetDebug
- bitcoinSignetRelease
- bitcoinTestnetDebug
- bitcoinTestnetRelease


### Supported architectures

The project produces an application supporting the following architectures:
- `x86_64`
- `arm64-v8a`
- `armeabi-v7a`


## Code formatting

The project uses [spotless](https://github.com/diffplug/spotless).

To format the code run:
```bash
./gradlew spotlessApply
```
and then commit the changes.


## Build

## APKs

Open the project in Android Studio to manually build APKs or use the
`build_apk.sh` script for automated builds.

With no parameters, the script builds the `Debug` versions of the APKs. To do
so run:
```bash
./build_apk.sh
```

The `Release` versions can instead be built by passing "release"
(case-insensitive) as the first parameter and the path to the keystore for
signing as the second parameter. As an example:
```bash
./build_apk.sh release ~/android-keystores/iriswallet.jks
```

Upon invocation, code will be linted and checked with spotless to make sure
there are no outstanding issues and it's well formatted. The build process gets
aborted if anything is out of order.

## Bundles

As a first step, lint and format the code:
```bash
./gradlew lint
./gradlew spotlessCheck
```
and proceed to fix anything that needs attention.

Once the code is ok, to build the release bundles for upload to the Play Store
run:
```bash
./gradlew bundleRelease
```
This will produce unsigned bundles in the `app/build/outputs/bundle/`
directory, one per variant.

To sign the bundles, run:
```bash
jarsigner -keystore <key_store> app/build/outputs/bundle/<variant>/<bundle_name>.aab <key_alias>
```
As an example, to sign the testnet variant using the key store located in
`~/android-keystores/iriswallet.jks` and key alias `upload`, run:
```bash
jarsigner -keystore ~/android-keystores/iriswallet.jks app/build/outputs/bundle/bitcoinTestnetRelease/app-bitcoinTestnet-release.aab upload
```

[bdk-kotlin]: https://github.com/bitcoindevkit/bdk-kotlin
[rgb-lib]: https://github.com/RGB-Tools/rgb-lib
[rgb-lib-kotlin]: https://github.com/RGB-Tools/rgb-lib-kotlin
