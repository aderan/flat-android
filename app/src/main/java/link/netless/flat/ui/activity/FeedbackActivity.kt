package link.netless.flat.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import link.netless.flat.R
import link.netless.flat.ui.activity.ui.theme.*
import link.netless.flat.ui.compose.BackTopAppBar
import link.netless.flat.ui.compose.FlatColumnPage
import link.netless.flat.ui.viewmodel.FeedbackViewModel

class FeedbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var text by rememberSaveable { mutableStateOf("") }
            val viewModel: FeedbackViewModel = viewModel()

            FlatColumnPage {
                BackTopAppBar(title = stringResource(R.string.title_feedback), { finish() });
                OutlinedTextField(
                    value = text,
                    { text = it },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .weight(2f),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = FlatColorBorder,
                        unfocusedIndicatorColor = FlatColorBorder
                    ),
                    placeholder = {
                        Text("请输入内容", style = FlatCommonTextStyle, color = FlatColorGray)
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 32.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(backgroundColor = FlatColorBlue),
                    shape = MaterialTheme.shapes.small,
                    onClick = { viewModel.uploadFeedback(text) }) {
                    Text(text = "提交", style = FlatCommonTextStyle, color = FlatColorWhite)
                }
            }
        }
    }
}