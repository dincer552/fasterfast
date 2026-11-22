# Mini Zıpla

Android için tek dokunuşla oynanan, engellerden zıplama oyunu.

- Görünen uygulama adı: **Mini Zıpla**
- Android paket adı: `com.dincer552.testpak2`
- İmza profili: `testpak2` (yalnızca bu uygulamaya ait)
- My App Store katalog dosyası: `testpak2.apk`
- Her `main` sürümü Android APK olarak derlenir, merkezi VM imzalayıcısıyla imzalanır ve imzası doğrulandıktan sonra GitHub Release'e yayımlanır.

## Oynanış

Oyuna başlamak için ekrana dokun. Karakter yerdeyken ekrana dokunarak zıpla. Engelleri geçtikçe skor artar; en iyi skor cihazda saklanır.

## Release doğrulaması

GitHub Actions, imzasız APK'yı derler; paket adı ve sürümü doğrular; GitHub OIDC ile bu depoya özel imza profilini hazırlar; imzalı APK üzerinde `apksigner verify`, `zipalign -c` ve tekrar paket kimliği kontrolü yapar. Yalnızca bütün adımlar başarılıysa `testpak2.apk` adıyla Release yayımlar.
