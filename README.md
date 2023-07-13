# solution-finder extended with pcsetup
## Setup Finder
This is an extension of knewjade's extremely popular sfinder project with support for automatic PC setup finding.
The vast majority of  of the work included here comes directly from knewjhade, and I take credit only for the parts
directly related to the `pcsetup` command. Simple instructions for use are copied below:

Basic usage: `java -jar sfinder-pcsetup-v0.2.jar pcsetup -sp LSZO -p [^O]p6 -d 180 -bksf v115@HhglBeRpEeglBtRpR4CehlBtR4NeAgH`

The command is `pcsetup`. Other normal sfinder features (like `percent`) *should* work, but I can't guarantee I didn't break something building this.

Specify the pieces you want to use to build the setup using `-sp`, in the above example we are looking at all 4p setups it is possible to build using the queue `LSZO`. IMPORTANT: This build is order dependent (with hold), so if you want to be completely completely sure you've covered everything, you may need to run it again with the setup pieces specified in a different order.

Specify the queue which will be used to solve the setup using `-p`, in the above example we are looking at a 4p setup for 7th which will take us to first, so well use the remaining 6 pieces from bag 10 to solve.

Finally, you can use `-bksf <fumen code>` to specify the best-known setup field for the given queue. If you have one available, this will greatly improve the speed, since it will limit the search depth of the rest of the setups.

Some other `sfinder` flags work as normal, for example `-d 180` to specify the use of 180 rotations, and `-f` in order to specify an initial field (all explored setups will place the setup pieces on top of the field specified in `-f`). Other stuff might work, it might not. Ping me or just try it if unsure.


## Now the original README:
テトリスの「パーフェクトクリア」「REN/Combo」「T-Spin」の手順などを探すためのツールです。  
探索条件には、7種のミノ(ITSZJLO) の組み合わせや地形を自由に設定できます。

最新版は、以下のリンクからダウンロードできます。  
https://github.com/knewjade/solution-finder/releases/latest

(GUIが入っているパッケージには、[@kitsune_fuchi](https://twitter.com/kitsune_fuchi) さん作成の補助GUIが入っています。 なお、補助GUIではpercent・path・util
figコマンドのみ対応しています)

※ プログラムの実行には、[Java8](https://www.java.com/ja/download/) 以降を実行できる環境が必要です

----

# 概要

solution-finderでは、次のような結果を得ることができます。

* ある地形からパーフェクトクリアできる確率を計算
* ある地形からパーフェクトクリアになるミノの置き方
* RENが最も続くようなミノの置き方
* T-Spinできるようになるミノの置き方
* 指定した地形と同じブロックの配置になるミノの置き方

solution-finderは、探索ツールとして次の特徴を持っています。

* 任意のフィールド・ミノ組み合わせを指定した探索が可能
* 探索時の回転法則はSRSに準拠
* マルチスレッドによる探索に対応
* 実行時にオプションを与えることで「ホールドあり・なし」「ハードドロップのみ」など細かい設定が可能
* フィールドの入力として [連続テト譜エディタ Ver 1.15a](http://fumen.zui.jp) のデータに対応

# 主な機能

* percent: ある地形からパーフェクトクリアできる確率を計算する
    - 7種のミノ(TIJLSZO) の様々な組み合わせでの探索が可能
    - 先頭nミノごとのパーフェクト成功確率もツリー形式で表示
    - パーフェクトができないツモ順を表示

* path: ある地形からパーフェクトまでの操作手順をすべて列挙する
    - 指定したミノの組み合わせから、パーフェクトまでの全パターンを列挙してファイルに出力
    - 2種類の結果を列挙して出力
    - 出力フォーマットは、テト譜リンクとCSVに対応

* setup: ある地形から指定したブロックを埋める操作手順をすべて列挙する
    - 指定したミノの組み合わせから、置くことができる全パターンを列挙してファイルに出力
    - ブロックを置いても置かなくても良いマージンエリアの設定が可能

* ren: ある地形からRENが続く手順を列挙する
  - 指定したミノ順から、RENを列挙してファイルに出力

* spin: ある地形からTスピンできる手順を列挙する

* cover: 指定したミノの置き場所通りに置くことができるミノ順をすべて列挙する

* util: solution-finderの機能を補助するユーティリティ
  - fig: テト譜をもとに画像を生成
  - fumen: 入力されたテト譜を変換して、新たなテト譜を標準出力に出力
  - seq: 入力されたパターンの``*``などをシーケンス（ツモ順）に展開

# ドキュメント

詳細は、以下のドキュメントをご参照ください。

https://knewjade.github.io/sfinder-docs


------

This software includes the work that is distributed in the Apache License 2.0

```
Apache Commons CLI
Copyright 2001-2017 The Apache Software Foundation

This product includes software developed at
The Apache Software Foundation (http://www.apache.org/).
```
