package com.floodsense.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floodsense.app.R
import com.floodsense.app.ui.components.FloodSenseLogo
import com.floodsense.app.ui.components.PrimaryGradientButton
import com.floodsense.app.ui.components.WaveBackground
import com.floodsense.app.ui.theme.Navy
import com.floodsense.app.ui.theme.PaleBlue
import com.floodsense.app.ui.theme.Teal
import com.floodsense.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private data class OnboardingSlide(
    val title: String,
    val body: String,
    val image: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val slides = listOf(
        OnboardingSlide(
            title = "Real-time Monitoring",
            body = "Monitor water level and rainfall in real time with IoT stations on the Kalu Ganga.",
            image = R.drawable.slide_realtime
        ),
        OnboardingSlide(
            title = "Smart Predictions",
            body = "AI models predict flood risk from water level and rainfall so you can act early.",
            image = R.drawable.slide_predictions
        ),
        OnboardingSlide(
            title = "Timely Alerts",
            body = "Receive instant alerts and recommended actions when risk rises.",
            image = R.drawable.slide_alerts
        )
    )
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()
    val lastPage = pagerState.currentPage == slides.lastIndex

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WaveBackground(strong = true)
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FloodSenseLogo(modifier = Modifier.weight(1f))
                TextButton(onClick = onFinished) {
                    Text("Skip", color = Teal, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) { page ->
                val slide = slides[page]
                Column(
                    Modifier.fillMaxSize().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(slide.image),
                        contentDescription = slide.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(28.dp))
                    Text(
                        slide.title,
                        color = Navy,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        slide.body,
                        color = TextSecondary,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                slides.indices.forEach { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (active) 10.dp else 8.dp)
                            .background(if (active) Teal else PaleBlue, CircleShape)
                    )
                }
            }
            PrimaryGradientButton(
                text = if (lastPage) "Get Started" else "Next",
                onClick = {
                    if (lastPage) {
                        onFinished()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
