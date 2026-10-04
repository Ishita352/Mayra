# Mayra Windows Agent — প্রাথমিক সেটআপ (বাংলা)

## বর্তমান অবস্থা

এটি Windows-এর জন্য প্রাথমিক local-only agent। এটি `PING`, `OPEN_NOTEPAD`, `OPEN_CALCULATOR`, এবং Windows / Network / Display / Sound Settings খোলার command সমর্থন করে। অন্যান্য declared capability এখনো চালু নয়। **ফোন থেকে remote command/pairing এখনো চালু নয়।**

## চালানোর নিয়ম

1. GitHub repository থেকে পুরো Mayra project ZIP হিসেবে ডাউনলোড করে Extract করুন।
2. Windows-এ Python 3 ইনস্টল করুন: https://www.python.org/downloads/windows/ । ইনস্টল করার সময় `Add python.exe to PATH` নির্বাচন করুন।
3. Extract করা project folder-এর `windows` ফোল্ডারে যান।
4. `start_mayra.bat`-এ double-click করুন।
5. Agent চালু হলে কোনো installation/setup password বা date-based code চাইবে না।
6. Agent চলতে থাকবে। বন্ধ করতে Command Prompt window বন্ধ করুন বা `Ctrl+C` চাপুন।

## গুরুত্বপূর্ণ নিরাপত্তা ও সীমাবদ্ধতা

- বর্তমান agent `127.0.0.1:8765`-এ bind করে; তাই এটি শুধু ওই Windows কম্পিউটারের local machine থেকে সংযোগ গ্রহণ করে, ফোন থেকে নয়।
- এই সংস্করণে remote pairing, phone authentication, browser control, file search, এবং automatic startup এখনো implement করা হয়নি।
- অজানা source থেকে script চালাবেন না। নিজের trusted PC-তেই ব্যবহার করুন।

## পরবর্তী development step

LAN pairing-এর আগে phone UI, one-time pairing approval, authenticated request protocol, rate limiting, এবং connection shutdown/revoke flow যোগ করতে হবে। Remote access চালু করার আগে নিরাপত্তা পরীক্ষা আবশ্যক।
