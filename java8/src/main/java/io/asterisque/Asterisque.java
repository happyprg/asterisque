/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package io.asterisque;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Asterisque
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

import java.nio.charset.Charset;

/**
 * @author Takami Torao
 */
public final class Asterisque {

	// ==============================================================================================
	// コンストラクタ
	// ==============================================================================================
	/**
	 * コンストラクタはクラス内に隠蔽されています。
	 */
	private Asterisque() {
	}

	// ==============================================================================================
	// UTF-8
	// ==============================================================================================
	/**
	 * UTF-8 を表す文字セットです。
	 */
	public static final Charset UTF8 = Charset.forName("UTF-8");
}
