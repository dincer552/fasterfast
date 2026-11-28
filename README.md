# Sunny Sip Bar

Android için renkli, animasyonlu bir sahil kafesi işletme oyunu.

- Görünen uygulama adı: **Sunny Sip Bar**
- Android paket adı: `com.dincer552.testpak2` (mevcut kurulumların güncellenebilmesi için korunur)
- İmza profili: `testpak2` (mevcut merkezi imzalama anahtarı korunur)
- My App Store katalog dosyası: `testpak2.apk`

## Oynanış

- Sahil kafesine farklı karakterlerde müşteriler gelir; her müşterinin ayrı içecek siparişi ve sabır göstergesi vardır.
- Müşteriye dokunarak siparişini seç.
- Malzemeleri dokunarak bardağa ekle: buz, limon, çilek, kahve, süt, nane ve portakal.
- Dört tarif bulunur: limonata, çilek fizz, buzlu kahve ve tropik içecek.
- Üç malzemeyi doğru tarifte birleştirip **Karıştır** düğmesine bas; ardından **Müşteriye Ver** ile siparişi teslim et.
- Doğru siparişler coin kazandırır, seri servis bonusu verir ve her beş teslimatta seviye yükselir.
- Müşteriler beklerken sabırları azalır; siparişleri zamanında hazırlamak gerekir.
- Karakterler, deniz, palmiye, kafe tezgâhı, içecekler ve malzeme ekleme efektleri hareketlidir.

## Teknik ve yayın

Paket kimliği ve imza sertifikası mevcut Android kurulumlarının güncellenebilmesi için değiştirilmez. GitHub Actions, APK'yı derler; paket adı/sürüm doğrulamasını yapar; merkezi VM imzalayıcısı ile imzalar; doğrulamadan sonra `testpak2.apk` adıyla Release yayımlar.
