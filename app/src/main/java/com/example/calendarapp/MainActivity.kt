package com.example.calendarapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendarapp.ui.CalendarViewModel
import com.example.calendarapp.ui.theme.CalendarAppTheme
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalendarAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CalendarApp()
                }
            }
        }
    }
}

@Composable
private fun CalendarApp(
    viewModel: CalendarViewModel = viewModel(),
) {
    val todayMonth = remember { YearMonth.now() }
    val basePage = Int.MAX_VALUE / 2
    val selections by viewModel.selections.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = basePage,
        pageCount = { Int.MAX_VALUE },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val monthOffset = page - basePage
            val month = todayMonth.plusMonths(monthOffset.toLong())
            val selectedDays = selections[month.toString()].orEmpty()

            MonthPage(
                month = month,
                selectedDays = selectedDays,
                onDayClick = { day -> viewModel.toggleDay(month, day) },
            )
        }
    }
}

@Composable
private fun MonthPage(
    month: YearMonth,
    selectedDays: Set<Int>,
    onDayClick: (Int) -> Unit,
) {
    val monthLabel = remember(month) {
        month.format(DateTimeFormatter.ofPattern("yyyy\u5e74M\u6708", Locale.CHINA))
    }
    val cells = remember(month) { buildMonthCells(month) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = monthLabel,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "\u672c\u6708\u9ad8\u4eae\u5929\u6570: ${selectedDays.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(20.dp))
        WeekHeader()
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(cells) { cell ->
                if (cell == null) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                    )
                } else {
                    DayCell(
                        day = cell,
                        isSelected = selectedDays.contains(cell),
                        onClick = { onDayClick(cell) },
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekHeader() {
    val labels = listOf(
        "\u4e00",
        "\u4e8c",
        "\u4e09",
        "\u56db",
        "\u4e94",
        "\u516d",
        "\u65e5",
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
            )
        }
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

private fun buildMonthCells(month: YearMonth): List<Int?> {
    val firstDay = month.atDay(1)
    val offset = firstDay.dayOfWeek.mondayBasedIndex()
    val totalDays = month.lengthOfMonth()
    val result = MutableList<Int?>(offset) { null }

    for (day in 1..totalDays) {
        result += day
    }

    while (result.size % 7 != 0) {
        result += null
    }

    return result
}

private fun DayOfWeek.mondayBasedIndex(): Int {
    return when (this) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }
}
