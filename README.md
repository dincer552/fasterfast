# FasterFastMini

Android için kesintisiz hareketli arena savaş oyunu. Oyuncu, aynı arenada dolaşan 29 botla rekabet eder.

- Görünen uygulama adı: **FasterFastMini**
- Android paket adı: `com.dincer552.testpak2` (mevcut kurulumların güncellenebilmesi için korunur)
- İmza profili: `testpak2` (mevcut merkezi imzalama anahtarı korunur)
- My App Store katalog dosyası: `testpak2.apk` (mağaza kataloğu uyumluluğu için korunur)
- Her `main` sürümü Android APK olarak derlenir, merkezi VM imzalayıcısıyla imzalanır ve doğrulanır.

## Oynanış

- Arenada 1 oyuncu ve 29 bot bulunur. Bütün toplar sürekli hareket eder; harita sınırına çarpınca sekerek yön değiştirir.
- Toplar birbirinin içinden geçerken temas süresi boyunca sürekli hasar verir. Hasar her saniye hesaplanır; temas uzadıkça toplam hasar artar ve büyük top küçük topa daha ağır hasar verir.
- Botlar kendilerinden belirgin biçimde büyük rakipleri yakında gördüklerinde yem/parça aramayı bırakıp öncelikle kaçar.
- Oyuncu, ekranın sağ altındaki saydam joystick'i sürükleyerek yönünü belirler. Joystick bırakılınca top son yönünde ilerlemeyi sürdürür.
- Dikenler sürekli ve belirgin biçimde döner. Toplar yem ve kalıntı topladıkça büyür; yapay bir maksimum boyut sınırı yoktur.
- Her top otomatik olarak tek tek, parlak mermi topları halinde seri ateş eder; eski çizgi/ışın efekti kaldırılmıştır. Haritadaki geçici güçler 6,2 saniyeliğine üçlü atış, dörtlü saçılmalı atış, daha hızlı atış veya güçlü mermi sağlar. Büyük topların mermileri daha güçlü ve menzili daha uzundur.
- Rakip parçalanınca geriye puan ve büyüme sağlayan parçalar bırakır. Bunları ilk toplayan top kazanır; botlar da kalıntıların peşine düşer.
- Parçalanan oyuncu/bot kısa süre sonra yeniden doğar. Yeniden doğan top önceki puanının bir kısmını korur.
- Haritadaki çok küçük sabit yemler yalnızca az miktarda puan kazandırır.
- Rakip kalıntısı toplamadan uzun süre kalan topların puanı zamanla azalır.
- Özel nesneler geçici güç verir: **uzun menzil**, **güçlü atış**, **hızlı ateş** ve **can yenileme**.
- Sağ üstte anlık ilk 5 sıralaması görünür.

## Teknik ve yayın

Paket kimliği ve imza sertifikası mevcut Android kurulumlarının güncellenebilmesi için değiştirilmez. GitHub Actions, APK'yı derler; paket adı/sürüm doğrulamasını yapar; merkezi VM imzalayıcısı ile imzalar; `apksigner verify` ve `zipalign -c` kontrollerinden sonra `testpak2.apk` adıyla Release yayımlar.
