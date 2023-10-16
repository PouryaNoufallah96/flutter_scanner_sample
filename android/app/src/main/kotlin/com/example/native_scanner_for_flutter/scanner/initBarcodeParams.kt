package com.example.native_scanner_for_flutter.scanner

import android.content.Context
import android.device.scanner.configuration.PropertyID
import android.device.scanner.configuration.Symbology
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import kotlin.collections.set

fun Context.initBarcodeParams(vararg args: Symbology): HashMap<String, BarcodeHolder> {
    val barcode = BarcodeHolder()
    val configBarcode = hashMapOf<String, BarcodeHolder>()
    configBarcode.clear()
    barcode.apply {
        mBarcodeEnable = CheckBoxPreference(this@initBarcodeParams)
        args.forEach { symbology ->
            when (symbology) {
                Symbology.AZTEC -> {
                    mParaIds = intArrayOf(PropertyID.AZTEC_ENABLE)
                    mParaKeys = arrayOf("AZTEC_ENABLE")
                    configBarcode[Symbology.AZTEC.toString()] = barcode
                }

                Symbology.CHINESE25 -> {
                    mParaIds = intArrayOf(PropertyID.C25_ENABLE)
                    mParaKeys = arrayOf("C25_ENABLE")
                    configBarcode[Symbology.CHINESE25.toString()] = barcode
                }

                Symbology.CODABAR -> {
                    mBarcodeLength1 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeLength2 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeNOTIS = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeCLSI = CheckBoxPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.CODABAR_ENABLE,
                        PropertyID.CODABAR_LENGTH1,
                        PropertyID.CODABAR_LENGTH2,
                        PropertyID.CODABAR_NOTIS,
                        PropertyID.CODABAR_CLSI,
                    )
                    mParaKeys = arrayOf(
                        "CODABAR_ENABLE",
                        "CODABAR_LENGTH1",
                        "CODABAR_LENGTH2",
                        "CODABAR_NOTIS",
                        "CODABAR_CLSI",
                    )
                    configBarcode[Symbology.CODABAR.toString()] = barcode
                }

                Symbology.CODE11 -> {
                    mBarcodeEnable = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeLength1 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeLength2 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeCheckDigit = ListPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.CODE11_ENABLE,
                        PropertyID.CODE11_LENGTH1,
                        PropertyID.CODE11_LENGTH2,
                        PropertyID.CODE11_SEND_CHECK,
                    )
                    mParaKeys = arrayOf(
                        "CODE11_ENABLE",
                        "CODE11_LENGTH1",
                        "CODE11_LENGTH2",
                        "CODE11_SEND_CHECK",
                    )
                    configBarcode[Symbology.CODE11.toString()] = barcode
                }

                Symbology.CODE32 -> {
                    mParaIds = intArrayOf(PropertyID.CODE32_ENABLE)
                    mParaKeys = arrayOf("CODE32_ENABLE")
                    configBarcode[Symbology.CODE32.toString()] = barcode
                }

                Symbology.CODE39 -> {
                    mBarcodeLength1 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeLength2 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeChecksum = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeSendCheck = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeFullASCII = CheckBoxPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.CODE39_ENABLE,
                        PropertyID.CODE39_LENGTH1,
                        PropertyID.CODE39_LENGTH2,
                        PropertyID.CODE39_ENABLE_CHECK,
                        PropertyID.CODE39_SEND_CHECK,
                        PropertyID.CODE39_FULL_ASCII,
                    )
                    mParaKeys = arrayOf(
                        "CODE39_ENABLE",
                        "CODE39_LENGTH1",
                        "CODE39_LENGTH2",
                        "CODE39_ENABLE_CHECK",
                        "CODE39_SEND_CHECK",
                        "CODE39_FULL_ASCII",
                    )
                    configBarcode[Symbology.CODE39.toString()] = barcode
                }

                Symbology.CODE93 -> {
                    mBarcodeLength1 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeLength2 = EditTextPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.CODE93_ENABLE,
                        PropertyID.CODE93_LENGTH1,
                        PropertyID.CODE93_LENGTH2,
                    )
                    mParaKeys = arrayOf(
                        "CODE93_ENABLE",
                        "CODE93_LENGTH1",
                        "CODE93_LENGTH2",
                    )
                    configBarcode[Symbology.CODE93.toString()] = barcode
                }

                Symbology.CODE128 -> {
                    mBarcodeLength1 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeLength2 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeISBT = CheckBoxPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.CODE128_ENABLE,
                        PropertyID.CODE128_LENGTH1,
                        PropertyID.CODE128_LENGTH2,
                        PropertyID.CODE128_CHECK_ISBT_TABLE,
                    )
                    mParaKeys = arrayOf(
                        "CODE128_ENABLE",
                        "CODE128_LENGTH1",
                        "CODE128_LENGTH2",
                        "CODE128_CHECK_ISBT_TABLE",
                    )
                    configBarcode[Symbology.CODE128.toString()] = barcode
                }

                Symbology.COMPOSITE_CC_AB -> {
                    mParaIds = intArrayOf(PropertyID.COMPOSITE_CC_AB_ENABLE)
                    mParaKeys = arrayOf("COMPOSITE_CC_AB_ENABLE")
                    configBarcode[Symbology.COMPOSITE_CC_AB.toString()] = barcode
                }

                Symbology.COMPOSITE_CC_C -> {
                    mParaIds = intArrayOf(PropertyID.COMPOSITE_CC_C_ENABLE)
                    mParaKeys = arrayOf("COMPOSITE_CC_C_ENABLE")
                    configBarcode[Symbology.COMPOSITE_CC_C.toString()] = barcode
                }

                Symbology.DATAMATRIX -> {
                    mParaIds = intArrayOf(PropertyID.DATAMATRIX_ENABLE)
                    mParaKeys = arrayOf("DATAMATRIX_ENABLE")
                    configBarcode[Symbology.DATAMATRIX.toString()] = barcode
                }

                Symbology.DISCRETE25 -> {
                    mParaIds = intArrayOf(PropertyID.D25_ENABLE)
                    mParaKeys = arrayOf("D25_ENABLE")
                    configBarcode[Symbology.DISCRETE25.toString()] = barcode
                }

                Symbology.EAN8 -> {
                    mParaIds = intArrayOf(PropertyID.EAN8_ENABLE)
                    mParaKeys = arrayOf("EAN8_ENABLE")
                    configBarcode[Symbology.EAN8.toString()] = barcode
                }

                Symbology.EAN13 -> {
                    mBarcodeBookLand = CheckBoxPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(PropertyID.EAN13_ENABLE, PropertyID.EAN13_BOOKLANDEAN)
                    mParaKeys = arrayOf("EAN13_ENABLE", "EAN13_BOOKLANDEAN")
                    configBarcode[Symbology.EAN13.toString()] = barcode
                }

                Symbology.GS1_14 -> {
                    mParaIds = intArrayOf(PropertyID.GS1_14_ENABLE)
                    mParaKeys = arrayOf("GS1_14_ENABLE")
                    configBarcode[Symbology.GS1_14.toString()] = barcode
                }

                Symbology.GS1_128 -> {
                    mParaIds = intArrayOf(PropertyID.CODE128_GS1_ENABLE)
                    mParaKeys = arrayOf("CODE128_GS1_ENABLE")
                    configBarcode[Symbology.GS1_128.toString()] = barcode
                }

                Symbology.GS1_EXP -> {
                    mBarcodeLength1 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeLength2 = EditTextPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.GS1_EXP_ENABLE,
                        PropertyID.GS1_EXP_LENGTH1,
                        PropertyID.GS1_EXP_LENGTH2,
                    )
                    mParaKeys = arrayOf("GS1_EXP_ENABLE", "GS1_EXP_LENGTH1", "GS1_EXP_LENGTH2")
                    configBarcode[Symbology.GS1_EXP.toString()] = barcode
                }

                Symbology.GS1_LIMIT -> {
                    mParaIds = intArrayOf(PropertyID.GS1_LIMIT_ENABLE)
                    mParaKeys = arrayOf("GS1_LIMIT_ENABLE")
                    configBarcode[Symbology.GS1_LIMIT.toString()] = barcode
                }

                Symbology.INTERLEAVED25 -> {
                    mBarcodeLength1 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeLength2 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeChecksum = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeSendCheck = CheckBoxPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.I25_ENABLE,
                        PropertyID.I25_LENGTH1,
                        PropertyID.I25_LENGTH2,
                        PropertyID.I25_ENABLE_CHECK,
                        PropertyID.I25_SEND_CHECK,
                    ); mParaKeys =
                        arrayOf(
                            "I25_ENABLE",
                            "I25_LENGTH1",
                            "I25_LENGTH2",
                            "I25_ENABLE_CHECK",
                            "I25_SEND_CHECK",
                        )
                    configBarcode[Symbology.INTERLEAVED25.toString()] = barcode
                }

                Symbology.MATRIX25 -> {
                    mParaIds = intArrayOf(PropertyID.M25_ENABLE)
                    mParaKeys = arrayOf("M25_ENABLE")
                    configBarcode[Symbology.MATRIX25.toString()] = barcode
                }

                Symbology.MAXICODE -> {
                    mParaIds = intArrayOf(PropertyID.MAXICODE_ENABLE)
                    mParaKeys = arrayOf("MAXICODE_ENABLE")
                    configBarcode[Symbology.MAXICODE.toString()] = barcode
                }

                Symbology.MICROPDF417 -> {
                    mParaIds = intArrayOf(PropertyID.MICROPDF417_ENABLE)
                    mParaKeys = arrayOf("MICROPDF417_ENABLE")
                    configBarcode[Symbology.MICROPDF417.toString()] = barcode
                }

                Symbology.MSI -> {
                    mBarcodeLength1 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeLength2 = EditTextPreference(this@initBarcodeParams)
                    mBarcodeSecondChecksum = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeSendCheck = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeSecondChecksumMode = CheckBoxPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.MSI_ENABLE,
                        PropertyID.MSI_LENGTH1,
                        PropertyID.MSI_LENGTH2,
                        PropertyID.MSI_REQUIRE_2_CHECK,
                        PropertyID.MSI_SEND_CHECK,
                        PropertyID.MSI_CHECK_2_MOD_11,
                    )
                    mParaKeys = arrayOf(
                        "MSI_ENABLE",
                        "MSI_LENGTH1",
                        "MSI_LENGTH2",
                        "MSI_REQUIRE_2_CHECK",
                        "MSI_SEND_CHECK",
                        "MSI_CHECK_2_MOD_11",
                    )
                    configBarcode[Symbology.MSI.toString()] = barcode
                }

                Symbology.PDF417 -> {
                    mParaIds = intArrayOf(PropertyID.PDF417_ENABLE)
                    mParaKeys = arrayOf("PDF417_ENABLE")
                    configBarcode[Symbology.PDF417.toString()] = barcode
                }

                Symbology.QRCODE -> {
                    mParaIds = intArrayOf(PropertyID.QRCODE_ENABLE)
                    mParaKeys = arrayOf("QRCODE_ENABLE")
                    configBarcode[Symbology.QRCODE.toString()] = barcode
                }

                Symbology.TRIOPTIC -> {
                    mParaIds = intArrayOf(PropertyID.TRIOPTIC_ENABLE)
                    mParaKeys = arrayOf("TRIOPTIC_ENABLE")
                    configBarcode[Symbology.TRIOPTIC.toString()] = barcode
                }

                Symbology.UPCA -> {
                    mBarcodeChecksum = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeSystemDigit = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeConvertEAN13 = CheckBoxPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.UPCA_ENABLE,
                        PropertyID.UPCA_SEND_CHECK,
                        PropertyID.UPCA_SEND_SYS,
                        PropertyID.UPCA_TO_EAN13,
                    )
                    mParaKeys = arrayOf(
                        "UPCA_ENABLE",
                        "UPCA_SEND_CHECK",
                        "UPCA_SEND_SYS",
                        "UPCA_TO_EAN13",
                    )
                    configBarcode[Symbology.UPCA.toString()] = barcode
                }

                Symbology.UPCE -> {
                    mBarcodeChecksum = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeSystemDigit = CheckBoxPreference(this@initBarcodeParams)
                    mBarcodeConvertUPCA = CheckBoxPreference(this@initBarcodeParams)
                    mParaIds = intArrayOf(
                        PropertyID.UPCE_ENABLE,
                        PropertyID.UPCE_SEND_CHECK,
                        PropertyID.UPCE_SEND_SYS,
                        PropertyID.UPCE_TO_UPCA,
                    )
                    mParaKeys = arrayOf(
                        "UPCE_ENABLE",
                        "UPCE_SEND_CHECK",
                        "UPCE_SEND_SYS",
                        "UPCE_TO_UPCA",
                    )
                    configBarcode[Symbology.UPCE.toString()] = barcode
                }

                Symbology.UPCE1 -> {
                    mParaIds = intArrayOf(PropertyID.UPCE1_ENABLE)
                    mParaKeys = arrayOf("UPCE1_ENABLE")
                    configBarcode[Symbology.UPCE1.toString()] = barcode
                }

                Symbology.NONE -> TODO()
                Symbology.COMPOSITE_TLC_39 -> TODO()
                Symbology.ISBT128 -> TODO()
                Symbology.CODE49 -> TODO()
                Symbology.TELEPEN -> TODO()
                Symbology.CODABLOCK_A -> TODO()
                Symbology.CODABLOCK_F -> TODO()
                Symbology.NEC25 -> TODO()
                Symbology.KOREA_POST -> TODO()
                Symbology.MICROQR -> TODO()
                Symbology.CANADA_POST -> TODO()
                Symbology.POSTAL_PLANET -> TODO()
                Symbology.POSTAL_POSTNET -> TODO()
                Symbology.POSTAL_4STATE -> TODO()
                Symbology.POSTAL_UPUFICS -> TODO()
                Symbology.POSTAL_ROYALMAIL -> TODO()
                Symbology.POSTAL_AUSTRALIAN -> TODO()
                Symbology.POSTAL_KIX -> TODO()
                Symbology.POSTAL_JAPAN -> TODO()
                Symbology.HANXIN -> TODO()
                Symbology.DOTCODE -> TODO()
                Symbology.POSTAL_UK -> TODO()
                Symbology.COMPOSITE_CC_B -> TODO()
                Symbology.MicroQR -> TODO()
                Symbology.GRIDMATRIX -> TODO()
                Symbology.CouponCode -> TODO()
            }
        }
    }
    return configBarcode
}