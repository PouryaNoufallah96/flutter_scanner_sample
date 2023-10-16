package com.example.native_scanner_for_flutter.scanner

import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference

class BarcodeHolder {

    var mBarcodeEnable                 : CheckBoxPreference? = null
    var mBarcodeLength1                : EditTextPreference? = null
    var mBarcodeLength2                : EditTextPreference? = null
    var mBarcodeNOTIS                  : CheckBoxPreference? = null
    var mBarcodeCLSI                   : CheckBoxPreference? = null
    var mBarcodeISBT                   : CheckBoxPreference? = null
    var mBarcodeChecksum               : CheckBoxPreference? = null
    var mBarcodeSendCheck              : CheckBoxPreference? = null
    var mBarcodeFullASCII              : CheckBoxPreference? = null
    var mBarcodeCheckDigit             : ListPreference?     = null
    var mBarcodeBookLand               : CheckBoxPreference? = null
    var mBarcodeSecondChecksum         : CheckBoxPreference? = null
    var mBarcodeSecondChecksumMode     : CheckBoxPreference? = null
    var mBarcodePostalCode             : ListPreference?     = null
    var mBarcodeSystemDigit            : CheckBoxPreference? = null
    var mBarcodeConvertEAN13           : CheckBoxPreference? = null
    var mBarcodeConvertUPCA            : CheckBoxPreference? = null
    var mBarcodeEnable25DigitExtensions: CheckBoxPreference? = null
    var mBarcodeDPM                    : CheckBoxPreference? = null
    var mParaIds                       : IntArray?           = null
    var mParaKeys                      : Array<String>?      = null
}