/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package io.asterisque;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Control
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Control メッセージはフレームワークによって他のメッセージより優先して送信されます。
 *
 * @author Takami Torao
 */
public final class Control extends Message {

	public static final byte Close = 1;

	// ==============================================================================================
	// ID
	// ==============================================================================================
	/**
	 * 制御コードです。
	 */
	public final byte code;

	// ==============================================================================================
	// データ
	// ==============================================================================================
	/**
	 * 制御コードに対するデータを表すバイト配列です。
	 */
	public final byte[] data;

	// ==============================================================================================
	// コンストラクタ
	// ==============================================================================================
	/**
	 * Control メッセージを構築します。
	 */
	public Control(byte code, byte[] data){
		super((short)0);
		if(data == null){
			throw new NullPointerException("data is null");
		}
		this.code = code;
		this.data = data;
	}

	// ==============================================================================================
	// インスタンスの文字列化
	// ==============================================================================================
	/**
	 * このインスタンスを文字列化します。
	 */
	@Override
	public String toString(){
		return "Control(0x" + String.format("%02X", code & 0xFF) + "," + Debug.toString(data) + ")";
	}
}
