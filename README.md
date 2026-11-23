# FasterFastMini

Android için kesintisiz hareketli arena savaş oyunu. Oyuncu, aynı arenada dolaşan 29 botla rekabet eder.

- Görünen uygulama adı: **FasterFastMini**
- Android paket adı: `com.dincer552.testpak2` (mevcut kurulumların güncellenebilmesi için korunur)
- İmza profili: `testpak2` (mevcut merkezi imzalama anahtarı korunur)
- My App Store katalog dosyası: `testpak2.apk` (mağaza kataloğu uyumluluğu için korunur)
- Her `main` sürümü Android APK olarak derlenir, merkezi VM imzalayıcısıyla imzalanır ve doğrulanır.

## Oynanış

- Arenada 1 oyuncu ve 29 bot bulunur. Bütün toplar sürekli hareket eder; harita sınırına çarpınca sekerek yön değiştirir.
- Oyuncu, ekranın sağ altındaki saydam joystick'i sürükleyerek yönünü belirler. Joystick bırakılınca top son yönünde ilerlemeyi sürdürür.
- Her top baktığı yöne otomatik ateş eder. Sağ alt joystick yalnızca hareket yönünü kontrol eder; ateş otomatik kalır. Menzil ve mermi hasarı topun büyüklüğüne göre artar; büyük toplar daha yavaş hareket eder.
- Rakip parçalanınca geriye puan ve büyüme sağlayan parçalar bırakır. Bunları ilk toplayan top kazanır; botlar da kalıntıların peşine düşer.
- Parçalanan oyuncu/bot kısa süre sonra yeniden doğar. Yeniden doğan top önceki puanının bir kısmını korur.
- Haritadaki çok küçük sabit yemler yalnızca az miktarda puan kazandırır.
- Rakip kalıntısı toplamadan uzun süre kalan topların puanı zamanla azalır.
- Özel nesneler geçici güç verir: **uzun menzil**, **güçlü atış**, **hızlı ateş** ve **can yenileme**.
- Sağ üstte anlık ilk 5 sıralaması görünür.

## Teknik ve yayın

Paket kimliği ve imza sertifikası mevcut Android kurulumlarının güncellenebilmesi için değiştirilmez. GitHub Actions, APK'yı derler; paket adı/sürüm doğrulamasını yapar; merkezi VM imzalayıcısı ile imzalar; `apksigner verify` ve `zipalign -c` kontrollerinden sonra `testpak2.apk` adıyla Release yayımlar.
