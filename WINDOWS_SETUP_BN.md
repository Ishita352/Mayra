# Mayra Windows Agent — Windows 10 সেটআপ (বাংলা)

## বর্তমান অবস্থা

Mayra-এর Windows companion এখন **trusted-LAN pairing mode** সমর্থন করে। Android ফোন ও Windows 10 PC একই trusted Wi-Fi/hotspot-এ থাকলে one-time pairing code দিয়ে authenticated session তৈরি করা যায়।

Windows-side allowlist-এ PING, Notepad, Calculator, Windows/Network/Display/Sound Settings, PC status, security status, browser opening এবং সীমিত media controls আছে। Arbitrary shell/command execution allowlist-এ নেই।

## গুরুত্বপূর্ণ

- Mayra-এর নিজের installation/setup/date-based password নেই।
- Pairing-এর জন্য one-time code এবং owner approval ব্যবহৃত হয়।
- Session token Windows-এ persist করে এবং explicit revoke না হওয়া পর্যন্ত restart-এর পরও login state রাখা যায়।
- LAN transport TLS-encrypted নয়। তাই শুধু নিজের trusted/private Wi-Fi বা hotspot-এ ব্যবহার করুন।
- Internet/public network-এ agent expose করবেন না।

## Windows 10-এ চালানো

### Option 1 — শুধু PC-তে local mode

`windows/start_mayra.bat` চালান। এটি loopback-only mode ব্যবহার করে।

### Option 2 — Android ↔ Windows trusted LAN mode

`windows/start_mayra_lan.bat` চালান। এটি স্পষ্টভাবে `MAYRA_AGENT_HOST=0.0.0.0` এবং `MAYRA_AGENT_ALLOW_LAN=1` সেট করে trusted LAN listener চালু করে।

তারপর:

1. Windows 10 PC এবং Android একই trusted Wi-Fi/hotspot-এ রাখুন।
2. Windows agent-এর Command Prompt-এ দেখানো **6-digit PAIRING CODE** নিন।
3. Mayra Android → **COMPUTER** খুলুন।
4. Windows PC-এর LAN IP এবং port `8765` দিন।
5. **Send Pair Request** চাপুন।
6. Windows Command Prompt-এ `PAIR <6-digit-code>` লিখে owner approval দিন।
7. Android-এ **Complete Pairing** চাপুন।
8. সফল হলে Android session token সংরক্ষণ করবে এবং trusted Windows session পুনরুদ্ধারের চেষ্টা করবে।
9. সংযোগ বন্ধ করতে Android-এর **Revoke** ব্যবহার করুন বা Windows agent-এ `REVOKE` দিন।

### Quick Owner Link

Windows agent একটি অস্থায়ী **8-digit Quick Owner Link code** দেখায়। Android-এর COMPUTER screen থেকে এটি ব্যবহার করে one-time quick pairing করা যায়।

## নিরাপত্তা

- Unknown/untrusted PC-তে script চালাবেন না।
- Public Wi-Fi-তে LAN mode চালু করবেন না।
- Pairing শেষ হলে প্রয়োজন না থাকলে LAN agent বন্ধ রাখুন।
- Session revoke করলে পুরনো token আর ব্যবহার করা যাবে না।
- Mayra-এর command allowlist arbitrary shell execution অনুমতি দেয় না।

## বর্তমান সীমাবদ্ধতা

Android-এর Windows session/pairing transport এবং Windows-side authenticated command path implemented। তবে **বাস্তব Android ফোন + Windows 10 PC-তে end-to-end physical test** এই CI environment থেকে করা সম্ভব নয়। তাই source/CI সফল হলেও আপনার নিজের দুই ডিভাইসে প্রথম pairing test এখনও প্রয়োজন।

## Developer verification

Python 3:

```sh
python -m unittest discover -s tests -v
```

Android:

```sh
gradle test --no-daemon
gradle assembleDebug --no-daemon
```

Build success মানে APK build/CI verification সফল; এটি নিজে থেকে physical phone installation বা Windows LAN test-এর প্রমাণ নয়।