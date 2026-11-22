# FasterFastMini

Android arena-survival oyunu. Tek oyuncu, 29 yapay zekâ rakibe karşı aynı haritada mücadele eder.

- Görünen uygulama adı: **FasterFastMini**
- Android paket adı: `com.dincer552.testpak2` (mevcut kurulumların güncellenebilmesi için korunur)
- İmza profili: `testpak2` (mevcut merkezi imzalama anahtarı korunur)
- My App Store katalog dosyası: `testpak2.apk` (mağaza kataloğu uyumluluğu için korunur)
- Her `main` sürümü Android APK olarak derlenir, merkezi VM imzalayıcısıyla imzalanır ve imzası doğrulandıktan sonra GitHub Release'e yayımlanır.

## Oynanış

- Arenada 1 ana oyuncu ve aynı anda 29 bot bulunur.
- Ekrana dokunarak hedef noktaya ilerle; sürükleyerek hareket yönünü değiştir.
- Bir botun üzerine art arda iki kez dokunarak saldır. Saldıran 18 enerji harcar; isabet alan savunan 9 enerji kaybeder.
- Saldırılar hedefe doğru ilerleyen, yılan gibi dalgalı bir enerji çizgisi olarak görünür.
- Botlar da birbirlerine ve ana oyuncuya saldırır; saldırılardan kaçınır ve enerji toplamak için yem arar.
- Soluk renkli, hareketsiz yemler enerji kazandırır. Hareket etmek enerji tüketir.
- Oyuncular ve botlar canlı renklerle, hareketli dikenli halkalarla gösterilir.

## Release doğrulaması

GitHub Actions, imzasız APK'yı derler; paket adı ve sürümü doğrular; GitHub OIDC ile bu depoya özel imza profilini hazırlar; imzalı APK üzerinde `apksigner verify`, `zipalign -c` ve tekrar paket kimliği kontrolü yapar. Yalnızca bütün adımlar başarılıysa mağaza kataloğuyla uyumlu `testpak2.apk` adıyla Release yayımlar.
