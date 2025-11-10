# SmartNotifier-Rev1

**SmartNotifier-Rev1** は、ChatGPTアプリから届く通知音を、通知内容のキーワードに応じてカスタマイズできる Android アプリです。  
たとえば「休憩開始」という通知にだけ特別な音声を再生する、といった設定が可能です。

---

## 🧩 動作の仕組み

1. ChatGPTアプリから通知が届くと、SmartNotifier がそのタイトルまたは本文を検出します。
2. 設定したキーワードに一致するルールが見つかると、元の通知をキャンセルし、指定した通知音で新しい通知を即座に発行します。

これにより、ChatGPTのスケジュール通知を、任意のサウンドや音声で知らせることができます。

---

## 💬 ChatGPTの通知について

### 通知の仕組み

ChatGPTでは、プロンプトで通知のタイトルや本文を指定し、タスクとしてスケジュールできます。

**プロンプト例：**
> 月曜日から金曜日で毎時10時から16時の50分に通知の休憩開始をスケジュールして下さい。

**ChatGPTの応答：**
> 設定できました。  
> 平日（月〜金）の 10:50, 11:50, 12:50, 13:50, 14:50, 15:50, 16:50 に「休憩を始めて」と通知します。

ChatGPTは指定時刻に「休憩開始」や「休憩を始めて」といったタイトル・本文で通知を発行します。  
SmartNotifier はこれを監視し、該当するキーワードが含まれる場合、指定した通知音で再通知します。

> ※ スマホアプリでは設定できません。Web版ChatGPTで通知スケジュールを設定してください。

---

## 🔧 必要な設定

### ChatGPTの通知を有効にする

1. スマホの設定から「アプリ」→「すべてのアプリを表示」を開き、リストから **ChatGPT** を選択します。
2. 「通知」をタップし、「ChatGPTのすべての通知」を有効にします。  
   （機種によってメニュー名が異なる場合があります。）

---

### SmartNotifier の権限設定

このアプリを利用するには、次の2つの権限が必要です。

- **通知へのアクセス**
- **通知の権限**

メイン画面のスイッチをONにすると、必要な権限を許可する画面が自動で表示されます。  
権限を許可するまではスイッチはONにならないため、最低3回ほどタップが必要です。

権限を許可すると、アプリが ChatGPT の通知を監視できるようになります。

---

## ⚙️ ルールの設定

「設定」ボタンをタップして、最大10個までルールを登録できます。  
各ルールには以下の項目を設定します：

- **検索キーワード**：通知本文に含まれる文字列
- **カスタムサウンド**：一致したときに再生する通知音

例：
| キーワード | 通知音 |
|--------------|-------------|
| 休憩 | voice_break.mp3 |

> この場合、ChatGPTから「休憩を始めて」という通知が届くと、`voice_break.mp3` が再生されます。

**注意点：**
- ChatGPTアプリはバックグラウンドで動作している必要があります。
- 同時刻の通知が重なった場合、数分の遅延が発生することがあります。
- SmartNotifierはバックグラウンドで動作し、アプリを閉じても監視を続けます。

---

## 🔊 応用例：音声で通知する

通常の通知音は単調なメロディーが多く、用途によっては意味が伝わりにくいことがあります。  
ここでは、音声ファイルを使って「休憩開始」を音声で知らせる例を紹介します。

### 1. 音声ファイルを作る
1. [Voicemaker™](https://voicemaker.in/) にアクセスし、無料アカウントを作成します。
2. 「休憩開始です。作業を中止して休みましょう」など、好みの音声を生成します。
3. ダウンロード後、分かりやすい名前（例：`voice_break.mp3`）に変更します。

### 2. 通知音リストへの登録
1. スマホの「設定」→「音とバイブレーション」→「デフォルトの通知音」を開きます。
2. 下部の「端末内のファイル」から、作成した音声ファイルを選択します。  
   （機種によって表示が異なる場合があります。）

### 3. SmartNotifierでの設定
1. アプリの設定画面を開きます。
2. 「検索キーワード」に「休憩」を入力します。
3. 「通知音」欄をタップし、リストから `voice_break.mp3` を選択します。

これで、指定時刻に ChatGPT から「休憩開始」通知が届くと、  
「休憩開始です。作業を中止して休みましょう」と音声で再生されます。

---

## 🧠 特徴まとめ

- ChatGPTのスケジュール通知を活用し、柔軟な通知パターンを実現
- キーワードごとに最大10種類の通知音を設定可能
- バックグラウンド動作により、常時監視が可能
- イヤホン使用時でも目的の通知のみ再生できるため、誤鳴動を防止

---

## 🪪 ライセンス
本アプリは個人開発プロジェクト **SmartNotifier-Rev1** の一部として提供されています。  
ライセンス形態はリポジトリ内の `LICENSE` ファイルを参照してください。  
  

  
---

English explanation  

# SmartNotifier-Rev1

**SmartNotifier-Rev1** is an Android app that lets you customize ChatGPT notification sounds based on specific keywords.
For example, you can set a unique voice clip to play only when the notification says “Break time started.”

---

## 🧩 How It Works

1. When a ChatGPT app notification appears, SmartNotifier detects its title or body text.

2. If a registered keyword rule matches, the app cancels the original notification and immediately issues a new one with your chosen custom sound.

This allows you to make ChatGPT’s scheduled notifications audible in any sound or voice you prefer.

---

## 💬 About ChatGPT Notifications
Notification Mechanism

In ChatGPT, you can schedule task notifications by defining the title and message through a prompt.

**Example Prompt:**

> Please schedule a “Start break” notification every weekday at 10:50, 11:50, 12:50, 13:50, 14:50, 15:50, and 16:50.

**ChatGPT’s reply:**

> Done.
> I’ll remind you to “Start your break” at those times on weekdays.

ChatGPT sends these notifications at the specified times.
SmartNotifier monitors them, and when the text contains a matching keyword, it re-notifies you with the assigned sound.

> ※ Scheduling notifications is only available on ChatGPT Web, not the mobile app.

---

## 🔧 Required Settings
### Enable ChatGPT Notifications

1. Open your phone’s Settings → Apps → See all apps, then choose ChatGPT.
2. Tap Notifications and make sure “Allow all ChatGPT notifications” is turned on.
   (Menu names may differ depending on the device.)

### Grant SmartNotifier Permissions

This app requires two permissions:

- Notification access
- Post notifications

When you turn on the switch in the main screen, SmartNotifier will automatically display the permission request screens.
You may need to tap the switch several times (usually up to three) before the permissions are fully granted.

Once granted, SmartNotifier can monitor ChatGPT notifications.

---

## ⚙️ Setting Up Rules

Tap the Settings button to register up to 10 rules.
Each rule has the following items:

- **Search Keyword** – A string contained in the notification text
- **Custom Sound** – The notification sound to play when matched

Example:
| Keyword | Notification Sound |
|--------------|-------------|
| Break | voice_break.mp3 |

> In this case, when ChatGPT sends “Start your break,” SmartNotifier will play voice_break.mp3.

**Notes:**

- ChatGPT must be running in the background.
- If multiple notifications occur simultaneously, a short delay may happen.
- SmartNotifier continues monitoring even when closed.

---

## 🔊 Example: Voice Notifications

Typical notification sounds are monotone and can be unclear for specific tasks.
Here’s how to announce “Break time started” using a voice file.

### 1. Create the Voice File

1. Visit [Voicemaker™](https://voicemaker.in/) and create a free account.
2. Generate an audio message such as: “Break time started. Please stop working and take a rest.”
3. Download and rename it (e.g., voice_break.mp3).

### 2. Add It to Your Notification List

1. Go to Settings → Sound & Vibration → Default notification sound.
2. Choose “Files on device” and select your downloaded MP3.
   (Display options vary by device.)

### 3. Configure in SmartNotifier

1. Open SmartNotifier’s Settings screen.
2. Enter “Break” as the Keyword.
3. Select voice_break.mp3 for the Notification Sound.

Now, when ChatGPT sends “Start your break,” you’ll hear the voice clip say:
“Break time started. Please stop working and take a rest.”

---

## 🧠 Key Features

- Enhances ChatGPT’s scheduled notifications with flexible sound patterns
- Up to 10 custom sounds per keyword
- Runs in the background for continuous monitoring
- Prevents unwanted sounds—only your target notifications play, even with earphones on

## 🪪 License

This app is part of the personal development project SmartNotifier-Rev1.  
For license details, see the `LICENSE` file in the repository.
