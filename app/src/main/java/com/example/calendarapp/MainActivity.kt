package com.example.calendarapp

import android.os.Bundle
import android.widget.NumberPicker
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendarapp.ui.CalendarViewModel
import com.example.calendarapp.ui.theme.CalendarAppTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

private val rangeStartMonth: YearMonth = YearMonth.of(2025, 1)
private val rangeEndMonth: YearMonth = YearMonth.of(2050, 12)

private enum class HomeTab(val label: String) {
    CALENDAR("日历"),
    STATS("统计"),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalendarApp()
        }
    }
}

@Composable
private fun CalendarApp(
    viewModel: CalendarViewModel = viewModel(),
) {
    val systemDarkTheme = isSystemInDarkTheme()
    var darkTheme by rememberSaveable { mutableStateOf(systemDarkTheme) }
    val actualToday = remember { YearMonth.now() }
    val todayMonth = remember(actualToday) { actualToday.coerceToSupportedRange() }
    val selections by viewModel.selections.collectAsStateWithLifecycle()
    val availableYears = remember(selections, todayMonth) {
        viewModel.availableYears(todayMonth.year)
            .filter { viewModel.yearTotal(it) > 0 }
            .ifEmpty { listOf(todayMonth.year) }
    }

    var currentTab by remember { mutableStateOf(HomeTab.CALENDAR) }
    var currentMonth by remember { mutableStateOf(todayMonth) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var statsYear by remember { mutableIntStateOf(todayMonth.year) }
    var transitionDirection by remember { mutableIntStateOf(1) }

    CalendarAppTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        ),
                    )
                    .systemBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    when (currentTab) {
                        HomeTab.CALENDAR -> {
                            SwipeMonthContainer(
                                month = currentMonth,
                                direction = transitionDirection,
                                onSwipePrevious = {
                                    if (currentMonth > rangeStartMonth) {
                                        transitionDirection = -1
                                        currentMonth = currentMonth.minusMonths(1)
                                    }
                                },
                                onSwipeNext = {
                                    if (currentMonth < rangeEndMonth) {
                                        transitionDirection = 1
                                        currentMonth = currentMonth.plusMonths(1)
                                    }
                        },
                    ) { month ->
                        val pageSelectedDays = selections[month.toString()].orEmpty()
                        val pageSummary = remember(selections, month) {
                            viewModel.summaryFor(month)
                        }
                        DashboardPage(
                            month = month,
                            selectedDays = pageSelectedDays,
                            highlightCount = pageSummary.monthCount,
                            onMonthTitleClick = { showMonthPicker = true },
                            onDayClick = { day -> viewModel.toggleDay(month, day) },
                        )
                            }
                        }

                        HomeTab.STATS -> {
                            StatsScreen(
                                year = statsYear,
                                availableYears = availableYears,
                                monthlyCounts = remember(selections, statsYear) {
                                    viewModel.monthlyCountsForYear(statsYear)
                                },
                                yearTotal = remember(selections, statsYear) {
                                    viewModel.yearTotal(statsYear)
                                },
                                darkTheme = darkTheme,
                                onToggleTheme = { darkTheme = !darkTheme },
                                onYearSelected = { statsYear = it },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentTab == HomeTab.CALENDAR && currentMonth != todayMonth) {
                        TodayShortcutButton(
                            onClick = {
                                transitionDirection = if (currentMonth <= todayMonth) 1 else -1
                                currentMonth = todayMonth
                            },
                        )
                    }
                }

                BottomNavBar(
                    currentTab = currentTab,
                    onTabChange = { tab ->
                        currentTab = tab
                        if (tab == HomeTab.STATS) {
                            statsYear = if (currentMonth.year in availableYears) {
                                currentMonth.year
                            } else {
                                availableYears.last()
                            }
                        }
                    },
                )
            }

            if (showMonthPicker) {
                MonthPickerDialog(
                    currentMonth = currentMonth,
                    onDismiss = { showMonthPicker = false },
                    onConfirm = { targetMonth ->
                        transitionDirection = if (targetMonth >= currentMonth) 1 else -1
                        currentMonth = targetMonth
                        showMonthPicker = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SwipeMonthContainer(
    month: YearMonth,
    direction: Int,
    onSwipePrevious: () -> Unit,
    onSwipeNext: () -> Unit,
    content: @Composable (YearMonth) -> Unit,
) {
    var dragAmount by remember(month) { mutableFloatStateOf(0f) }
    var isDragging by remember(month) { mutableStateOf(false) }
    val previousMonth = remember(month) {
        if (month > rangeStartMonth) month.minusMonths(1) else null
    }
    val nextMonth = remember(month) {
        if (month < rangeEndMonth) month.plusMonths(1) else null
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val swipeThreshold = widthPx * 0.46f
        val pageGap = with(LocalDensity.current) { 18.dp.toPx() }
        val animatedOffset by animateFloatAsState(
            targetValue = dragAmount,
            animationSpec = if (isDragging) {
                snap()
            } else {
                spring(
                    dampingRatio = 0.9f,
                    stiffness = 650f,
                )
            },
            label = "month_drag_offset",
        )
        val showAdjacentMonths = isDragging || kotlin.math.abs(animatedOffset) > 1f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(month) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, drag ->
                            isDragging = true
                            val proposed = dragAmount + drag
                            dragAmount = when {
                                proposed > 0 && previousMonth == null -> proposed * 0.18f
                                proposed < 0 && nextMonth == null -> proposed * 0.18f
                                else -> proposed
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            when {
                                dragAmount > swipeThreshold && previousMonth != null -> {
                                    onSwipePrevious()
                                }

                                dragAmount < -swipeThreshold && nextMonth != null -> {
                                    onSwipeNext()
                                }

                                else -> {
                                    dragAmount = 0f
                                }
                            }
                            isDragging = false
                        },
                        onDragCancel = {
                            dragAmount = 0f
                            isDragging = false
                        },
                    )
                },
        ) {
            if (showAdjacentMonths && previousMonth != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = animatedOffset - widthPx - pageGap
                            alpha = (1f - (kotlin.math.abs(animatedOffset) / widthPx) * 0.12f)
                                .coerceIn(0.88f, 1f)
                        },
                ) {
                    content(previousMonth)
                }
            }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = animatedOffset
                        },
                ) {
                    content(month)
                }

            if (showAdjacentMonths && nextMonth != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = animatedOffset + widthPx + pageGap
                            alpha = (1f - (kotlin.math.abs(animatedOffset) / widthPx) * 0.12f)
                                .coerceIn(0.88f, 1f)
                        },
                ) {
                    content(nextMonth)
                }
            }
        }
    }
}

@Composable
private fun DashboardPage(
    month: YearMonth,
    selectedDays: Set<Int>,
    highlightCount: Int,
    onMonthTitleClick: () -> Unit,
    onDayClick: (Int) -> Unit,
) {
    val monthLabel = remember(month) {
        month.format(DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINA))
    }
    val cells = remember(month) { buildMonthCells(month) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CalendarHeroCard(
            month = month,
            monthLabel = monthLabel,
            highlightCount = highlightCount,
            cells = cells,
            selectedDays = selectedDays,
            onMonthTitleClick = onMonthTitleClick,
            onDayClick = onDayClick,
        )
    }
}

@Composable
private fun CalendarHeroCard(
    month: YearMonth,
    monthLabel: String,
    highlightCount: Int,
    cells: List<Int?>,
    selectedDays: Set<Int>,
    onMonthTitleClick: () -> Unit,
    onDayClick: (Int) -> Unit,
) {
    val today = LocalDate.now()
    val rows = remember(cells) { cells.chunked(7) }
    val weekLabels = listOf("一", "二", "三", "四", "五", "六", "日")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    ),
                )
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onMonthTitleClick)
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.94f),
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "本月犯错",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                        )
                        Text(
                            text = highlightCount.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                weekLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        week.forEach { cell ->
                            Box(modifier = Modifier.weight(1f)) {
                                if (cell == null) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f),
                                    )
                                } else {
                                    val isToday = today.dayOfMonth == cell &&
                                        today.monthValue == month.monthValue &&
                                        today.year == month.year
                                    DayCell(
                                        day = cell,
                                        isSelected = selectedDays.contains(cell),
                                        isToday = isToday,
                                        onClick = { onDayClick(cell) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayShortcutButton(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .shadow(12.dp, RoundedCornerShape(999.dp))
            .clip(RoundedCornerShape(999.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary,
                    ),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "今",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val border = if (isToday && !isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    } else {
        null
    }
    val backgroundBrush = if (isSelected) {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary,
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceBright,
                MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
        )
    }
    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(shape)
            .background(backgroundBrush)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.14f)),
            )
        }
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatsPage(
    year: Int,
    availableYears: List<Int>,
    monthlyCounts: List<Int>,
    yearTotal: Int,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onYearSelected: (Int) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(12.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    ),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "年度统计",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            YearChipRow(
                years = availableYears,
                selectedYear = year,
                onYearSelected = onYearSelected,
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "全年犯错总数",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                    )
                    Text(
                        text = yearTotal.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            MonthlyBarChart(
                monthlyCounts = monthlyCounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun StatsScreen(
    year: Int,
    availableYears: List<Int>,
    monthlyCounts: List<Int>,
    yearTotal: Int,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onYearSelected: (Int) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .shadow(12.dp, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceContainer,
                        ),
                    ),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "年度统计",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ThemeToggleButton(
                    darkTheme = darkTheme,
                    onClick = onToggleTheme,
                )
            }

            YearChipRow(
                years = availableYears,
                selectedYear = year,
                onYearSelected = onYearSelected,
            )

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.82f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "全年高亮总数",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                    )
                    Text(
                        text = yearTotal.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            MonthlyBarChart(
                monthlyCounts = monthlyCounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun ThemeToggleButton(
    darkTheme: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (darkTheme) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (darkTheme) "☾" else "☀",
            style = MaterialTheme.typography.headlineSmall,
            color = if (darkTheme) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
        )
    }
}

@Composable
private fun YearChipRow(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        years.forEach { year ->
            val selected = year == selectedYear
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (selected) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary,
                                ),
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                            )
                        },
                    )
                    .clickable { onYearSelected(year) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    text = "${year}年",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MonthlyBarChart(
    monthlyCounts: List<Int>,
    modifier: Modifier = Modifier,
) {
    val maxValue = monthlyCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
    val monthLabels = (1..12).map { it.toString() }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "月度对比",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(244.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                monthlyCounts.forEachIndexed { index, count ->
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    ) {
                        val ratio = count.toFloat() / maxValue.toFloat()
                        val barHeight = if (count == 0) 10.dp else (maxHeight * 0.68f) * ratio
                        val reservedTop = 28.dp
                        val reservedBottom = 30.dp
                        val gap = 4.dp
                        val spacerHeight = max(
                            0,
                            ((maxHeight - reservedTop - reservedBottom - gap - barHeight).value).toInt(),
                        ).dp

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(modifier = Modifier.height(spacerHeight))
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(gap))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.52f)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(primary, tertiary),
                                        ),
                                    ),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = monthLabels[index],
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    currentTab: HomeTab,
    onTabChange: (HomeTab) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HomeTab.entries.forEach { tab ->
                val selected = tab == currentTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (selected) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary,
                                    ),
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.surface,
                                    ),
                                )
                            },
                        )
                        .clickable { onTabChange(tab) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit,
) {
    var selectedYear by remember(currentMonth) { mutableIntStateOf(currentMonth.year) }
    var selectedMonth by remember(currentMonth) { mutableIntStateOf(currentMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(selectedYear, selectedMonth)) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = {
            Text(
                text = "滚动选择年月",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WheelPickerCard(
                        title = "年份",
                        modifier = Modifier.weight(1f),
                    ) {
                        WheelNumberPicker(
                            value = selectedYear,
                            range = 2025..2050,
                            wrap = false,
                            formatter = { "${it}年" },
                            onValueChange = { _, newValue ->
                                selectedYear = newValue
                            },
                        )
                    }
                    WheelPickerCard(
                        title = "月份",
                        modifier = Modifier.weight(1f),
                    ) {
                        WheelNumberPicker(
                            value = selectedMonth,
                            range = 1..12,
                            wrap = true,
                            formatter = { "${it}月" },
                            onValueChange = { _, newValue ->
                                selectedMonth = newValue
                            },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun WheelPickerCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            content()
        }
    }
}

@Composable
private fun WheelNumberPicker(
    value: Int,
    range: IntRange,
    wrap: Boolean,
    formatter: (Int) -> String,
    onValueChange: (oldValue: Int, newValue: Int) -> Unit,
) {
    val displayedValues = remember(range.first, range.last) {
        range.map(formatter).toTypedArray()
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        factory = { context ->
            NumberPicker(context).apply {
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                wrapSelectorWheel = wrap
            }
        },
        update = { picker ->
            picker.setOnValueChangedListener(null)
            picker.displayedValues = null
            picker.minValue = range.first
            picker.maxValue = range.last
            picker.wrapSelectorWheel = wrap
            picker.displayedValues = displayedValues
            if (picker.value != value) {
                picker.value = value
            }
            picker.setOnValueChangedListener { _, oldValue, newValue ->
                onValueChange(oldValue, newValue)
            }
        },
    )
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

private fun YearMonth.coerceToSupportedRange(): YearMonth {
    return when {
        this < rangeStartMonth -> rangeStartMonth
        this > rangeEndMonth -> rangeEndMonth
        else -> this
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 920, apiLevel = 34)
@Composable
private fun DashboardPreview() {
    CalendarAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DashboardPage(
                month = YearMonth.of(2026, 4),
                selectedDays = setOf(3, 8, 12, 19, 21, 28),
                highlightCount = 6,
                onMonthTitleClick = {},
                onDayClick = {},
            )
        }
    }
}
