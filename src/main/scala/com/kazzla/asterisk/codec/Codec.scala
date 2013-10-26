/*
 * Copyright (c) 2013 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package com.kazzla.asterisk.codec

import java.nio.ByteBuffer
import com.kazzla.asterisk.Message

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Codec
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * @author Takami Torao
 */
object Codec {

	// ==============================================================================================
	// 最大メッセージ長
	// ==============================================================================================
	/**
	 * シリアライズした 1 メッセージの最大バイナリ長です。IPv4 のデータ部最大長である 65,507 を表します。
	 */
	val MaxMessageSize = 65507
}

trait Codec {

	// ==============================================================================================
	// メッセージのエンコード
	// ==============================================================================================
	/**
	 * 指定されたメッセージをエンコードします。
	 * シリアライズ結果がメッセージの上限より大きい場合は [[com.kazzla.asterisk.codec.CodecException]] が発生
	 * します。
	 * @param msg エンコードするメッセージ
	 * @return エンコードされたメッセージ
	 */
	def encode(msg:Message):ByteBuffer

	// ==============================================================================================
	// メッセージのデコード
	// ==============================================================================================
	/**
	 * 指定されたメッセージをデコードします。
	 *
	 * このメソッドの呼び出しはデータを受信する都度行われます。従って、サブクラスはメッセージ全体を復元できるだけデー
	 * タを受信していない場合に None を返す必要があります。
	 *
	 * パラメータの [[java.nio.ByteBuffer]] の位置は次回の呼び出しまで維持されます。このためサブクラスは復元した
	 * メッセージの次の適切な読み出し位置を正しくポイントする必要があります。またメッセージを復元できるだけのデータを
	 * 受信していない場合には読み出し位置を変更すべきではありません。コーデック実装により無視できるバイナリが存在する
	 * 場合はバッファ位置を変更して None を返す事が出来ます。
	 *
	 * メッセージのデコードに失敗した場合は [[com.kazzla.asterisk.codec.CodecException]] が発生します。
	 *
	 * @param buffer デコードするメッセージ
	 * @return デコードしたメッセージ
	 */
	def decode(buffer:ByteBuffer):Option[Message]
}

/**
 * メッセージのエンコード/デコードに失敗した事を表す例外です。
 * @param msg 例外メッセージ
 * @param ex 下層の例外
 */
class CodecException(msg:String, ex:Throwable = null) extends Exception(msg, ex)