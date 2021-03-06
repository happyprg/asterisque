/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.asterisque.msg;

import org.asterisque.Debug;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Control
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * Control メッセージはフレームワークによって他のメッセージより優先して送信されます。
 *
 * @author Takami Torao
 */
public final class Control extends Message {

	/**
	 * バイナリストリーム上で {@code *Q} を表すために {@link org.asterisque.codec.Codec.Msg#Control} が
	 * {@code *}、{@code SyncConfig} が {@code Q} の値を取ります。
	 * SyncConfig のフィールドはヘルパークラス {@link org.asterisque.msg.SyncConfig} 経由でアクセスすること
	 * が出来ます。
	 */
	public static final byte SyncConfig = 'Q';

	/**
	 * セッションの終了を表す制御コード。有効なデータデーは付随しない。
	 */
	public static final byte Close = 'C';

	private static final byte[] Empty = new byte[0];

	// ==============================================================================================
	// 制御コード
	// ==============================================================================================
	/**
	 * 制御コードです。
	 */
	public final byte code;

	// ==============================================================================================
	// データ
	// ==============================================================================================
	/**
	 * コード値に付属するデータを表すバイナリです。
	 */
	public final byte[] data;

	// ==============================================================================================
	// コンストラクタ
	// ==============================================================================================
	/**
	 * Control メッセージを構築します。
	 */
	public Control(byte code, byte[] data){
		super((short)0 /* not used */);
		if(data == null){
			throw new NullPointerException("data is null");
		}
		this.code = code;
		this.data = data;
	}

	// ==============================================================================================
	// コンストラクタ
	// ==============================================================================================
	/**
	 * Control メッセージを構築します。
	 */
	public Control(byte code){
		super((short)0 /* not used */);
		this.code = code;
		this.data = Empty;
	}

	// ==============================================================================================
	// インスタンスの文字列化
	// ==============================================================================================
	/**
	 * このインスタンスを文字列化します。
	 */
	@Override
	public String toString(){
		return "Control(" + ((char)code) + "," + Debug.toString(data) + ")";
	}

}
