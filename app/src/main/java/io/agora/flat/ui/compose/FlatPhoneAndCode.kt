package io.agora.flat.ui.compose

import android.app.Activity
import android.content.Intent
import android.os.CountDownTimer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.agora.flat.Constants
import io.agora.flat.R
import io.agora.flat.data.model.Country
import io.agora.flat.ui.activity.CallingCodeActivity
import io.agora.flat.ui.theme.Red_6
import io.agora.flat.util.isValidPhone
import io.agora.flat.util.isValidSmsCode

@Composable
fun PhoneAndCodeArea(
    phone: String,
    onPhoneChange: (String) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    callingCode: String,
    onCallingCodeChange: (String) -> Unit,
    onSendCode: () -> Unit,
) {
    var isValidPhone by remember { mutableStateOf(true) }
    var isValidCode by remember { mutableStateOf(true) }

    var hasCodeFocused by remember { mutableStateOf(false) }
    var hasPhoneFocused by remember { mutableStateOf(false) }

    var remainTime by remember { mutableStateOf(0L) }
    val countDownTimer = remember {
        object : CountDownTimer(60_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainTime = millisUntilFinished / 1000
            }

            override fun onFinish() {
                remainTime = 0
            }
        }
    }

    val sendCodeEnable = remainTime == 0L && phone.isValidPhone()
    val sendCodeText = if (remainTime == 0L) stringResource(id = R.string.login_send_sms_code) else "${remainTime}s"
    val context = LocalContext.current

    val callingCodeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                val country: Country = it.data!!.getParcelableExtra(Constants.IntentKey.COUNTRY)!!
                onCallingCodeChange(country.cc)
            }
        }
    )

    Column(Modifier.padding(horizontal = 16.dp)) {
        FlatTextCaption(text = stringResource(id = R.string.bind_phone))
        Spacer(Modifier.height(4.dp))
        Row(Modifier.height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            val intent = Intent(context, CallingCodeActivity::class.java)
                            callingCodeLauncher.launch(intent)
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(8.dp))
                FlatTextBodyOne(text = callingCode)
                Image(painterResource(R.drawable.ic_login_arrow_down), contentDescription = "")
                Spacer(Modifier.width(8.dp))
            }
            BindPhoneTextField(
                value = phone,
                onValueChange = {
                    if (isValidPhone.not() && it.isValidPhone()) {
                        isValidPhone = true
                    }
                    onPhoneChange(it)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                onFocusChanged = {
                    if (hasPhoneFocused.not() && it.isFocused) {
                        hasPhoneFocused = true
                    }
                    if (hasPhoneFocused && it.isFocused.not()) {
                        isValidPhone = phone.isValidPhone()
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                placeholderValue = stringResource(R.string.login_phone_input_placeholder),
            )
        }
        if (isValidPhone) {
            FlatDivider(thickness = 1.dp)
        } else {
            FlatDivider(color = Red_6, thickness = 1.dp)
            FlatTextBodyTwo(stringResource(R.string.login_phone_invalid_tip), color = Red_6)
        }
        Spacer(Modifier.height(16.dp))
        FlatTextCaption(stringResource(R.string.verification_code))
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_login_sms_code), contentDescription = "")
            Spacer(Modifier.width(8.dp))
            BindPhoneTextField(
                value = code,
                onValueChange = {
                    if (it.length > 6) {
                        return@BindPhoneTextField
                    }
                    if (isValidCode.not() && it.isValidSmsCode()) {
                        isValidCode = true
                    }
                    onCodeChange(it)
                },
                modifier = Modifier
                    .height(40.dp)
                    .weight(1f),
                onFocusChanged = {
                    if (hasCodeFocused.not() && it.isFocused) {
                        hasCodeFocused = true
                    }

                    if (hasCodeFocused && it.isFocused.not()) {
                        isValidCode = code.isValidSmsCode()
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                placeholderValue = stringResource(R.string.login_code_input_placeholder),
            )
            TextButton(
                enabled = sendCodeEnable,
                onClick = {
                    countDownTimer.start()
                    onSendCode()
                },
            ) {
                FlatTextOnButton(text = sendCodeText)
            }
        }
        if (isValidCode) {
            FlatDivider(thickness = 1.dp)
        } else {
            FlatDivider(color = Red_6, thickness = 1.dp)
            FlatTextBodyTwo(stringResource(R.string.login_code_invalid_tip), color = Red_6)
        }
    }
}

@Composable
@Preview
private fun PhoneAndCodeAreaPreview() {
    PhoneAndCodeArea(
        phone = "",
        onPhoneChange = {},
        code = "",
        onCodeChange = {},
        callingCode = "+1",
        onCallingCodeChange = {},
        {},
    )
}
