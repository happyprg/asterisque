/*
 * Copyright (c) 2014 koiroha.org.
 * All sources and related resources are available under Apache License 2.0.
 * http://www.apache.org/licenses/LICENSE-2.0.html
*/
package org.asterisque;

// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// Tuple
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
/**
 * インデックスによるアクセスが可能なフィールドを持つタプルを表すインターフェースです。
 * Scala の Product や Tuple に相当します。
 *
 * @author Takami Torao
 */
public interface Tuple {

	// ==============================================================================================
	// 最大プロパティ数
	// ==============================================================================================
	/**
	 * シリアライズ可能なタプル型のプロパティ数上限です。
	 * Scala では case class のプロパティ数に相当します。
	 */
	public static int MaxFields = 0xFF;

	// ============================================================================================
	// スキーマ名
	// ============================================================================================
	/**
	 * この構造体の表すスキーマ名です。Java 系の言語ではクラス名に相当します。
	 */
	public String schema();

	// ============================================================================================
	// フィールド数
	// ============================================================================================
	/**
	 * この構造体のフィールド数を参照します。
	 */
	public int count();

	// ============================================================================================
	// フィールド値の参照
	// ============================================================================================
	/**
	 * 指定されたインデックスのフィールド値を参照します。
	 */
	public Object valueAt(int i);

}