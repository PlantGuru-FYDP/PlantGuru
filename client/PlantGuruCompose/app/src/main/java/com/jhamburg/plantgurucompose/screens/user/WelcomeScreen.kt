package com.jhamburg.plantgurucompose.screens.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.jhamburg.plantgurucompose.R
import com.jhamburg.plantgurucompose.ui.theme.PlantGuruComposeTheme
import com.jhamburg.plantgurucompose.ui.theme.RighteousFont

@Composable
fun WelcomeScreen(navController: NavController?) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val iconSize = screenWidth / 6
        val textSize = (iconSize*0.8).sp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_logo),
                contentDescription = "PlantGuru Icon",
                modifier = Modifier.size(iconSize.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "PlantGuru",
                style = TextStyle(
                    fontFamily = RighteousFont,
                    fontSize = textSize,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "The Intelligent Plant Care Solution",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                navController?.navigate("login")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                navController?.navigate("signup")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 24.dp),
        ) {
            Text("Sign Up")
        }
    }
}

@Preview(
    name = "Welcome Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun WelcomeScreenPreview() {
    PlantGuruComposeTheme {
        Surface {
            WelcomeScreen(navController = null)
        }
    }
}