# Inventory Material 3 Design Skill

## Overview

Master Material 3 UI/UX design principles specifically tailored for elegant, minimalist inventory applications like Craft Nook. This skill guides you in creating sophisticated, content-focused interfaces with a refined aesthetic.

## Core Design Philosophy

### 1. Focus on Content
**Priority**: Inventory data (name, quantity/stock) is the primary focus
- Display essential information prominently
- Remove or minimize secondary data (price, detailed specs) unless critical
- Use visual hierarchy to guide user attention to what matters most
- Reduce cognitive load by eliminating distractions

**Example**: In a card, the material name should be the largest text, stock quantity should be clearly visible, and optional fields like price should be subtle or hidden.

### 2. Elegant Typography
**Goals**: Modern, readable, and slightly playful font treatments
- Use Material 3 typography scale effectively
- Apply font weights strategically (SemiBold for emphasis, Regular for content)
- Pair serif/sans-serif combinations thoughtfully
- Maintain readability across all devices

**Recommended Typography**:
- **Headlines**: Material 3 `headlineSmall` or `titleLarge` (SemiBold weight)
- **Body Text**: Material 3 `bodySmall` or `bodyMedium` (Regular weight)
- **Labels**: Material 3 `labelSmall` or `labelMedium` (Medium weight)

**Best Practices**:
```kotlin
// Good: Emphasizes the material name
Text(
    text = material.name,
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.SemiBold
)

// Avoid: Too much competing information
Text("Name: ${material.name} | Stock: ${material.quantity} | Price: $$${material.price}")
```

### 3. Sophisticated Pastel Palette
**Concept**: Use soft, complementary colors instead of standard Material 3 defaults
- Replace harsh primary colors with muted, warm tones
- Create a cohesive color scheme using pastel variants
- Ensure sufficient contrast for accessibility
- Apply colors strategically to support content hierarchy

**Recommended Pastel Palette for Craft Nook**:
```kotlin
// Primary: Soft warm purple/lavender
val primary = Color(0xFFD4A8E8)          // Light lavender
val onPrimary = Color(0xFF3F2C47)        // Deep plum text

// Secondary: Soft sage/mint
val secondary = Color(0xFFB8E6D5)        // Mint green
val onSecondary = Color(0xFF2D4A40)      // Deep forest text

// Tertiary: Soft peach/salmon
val tertiary = Color(0xFFE8C4B0)         // Soft peach
val onTertiary = Color(0xFF4A3028)       // Deep brown text

// Surface variants: Soft neutral
val surfaceVariant = Color(0xFFF5E6F5)   // Light lavender-white
val errorContainer = Color(0xFFE8D4D4)   // Soft blush (instead of red)
```

**Color Application**:
- **Cards/Surfaces**: Use `surfaceVariant` (soft background)
- **Active States**: Use `primary` (soft lavender)
- **Category Badges**: Use `secondary` or `tertiary` for visual interest
- **Accents**: Apply pastels to FilterChips and highlights
- **Error States**: Use softer error colors (blush instead of bright red)

### 4. Micro-interactions
**Purpose**: Subtle animations that provide feedback without being distracting

**Key Interactions**:

#### a) Button Press Feedback
```kotlin
Button(
    onClick = { /* action */ },
    modifier = Modifier
        .animateClick() // Custom extension or use Compose's built-in press indication
        .size(36.dp),
    shape = RoundedCornerShape(8.dp)
) {
    Icon(...)
}

// Helper extension (optional)
fun Modifier.animateClick(): Modifier = this.then(
    Modifier.pointerInput(Unit) {
        detectTapGestures { }
    }
)
```

#### b) Chip Selection Animation
```kotlin
FilterChip(
    selected = isSelected,
    onClick = { /* select */ },
    label = { Text(category) },
    modifier = Modifier.animateContentSize() // Animate size changes
)
```

#### c) Card Elevation on Hover (Desktop) or Focus (Mobile)
```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .animateElevation(
            targetElevation = if (isHovered) 8.dp else 2.dp
        ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = 2.dp,
        pressedElevation = 4.dp
    )
) {
    // Content
}

// Helper extension
fun Modifier.animateElevation(targetElevation: Dp): Modifier {
    val elevation by animateDpAsState(targetValue = targetElevation)
    return this // Apply elevation through shadow
}
```

#### d) Quantity Update Feedback
```kotlin
// Animate quantity value change
val animatedQuantity by animateIntAsState(
    targetValue = material.quantity,
    animationSpec = spring(dampingRatio = 0.7f)
)

Text(
    text = "$animatedQuantity ${material.unit}",
    style = MaterialTheme.typography.bodySmall
)
```

#### e) Dialog Entrance/Exit
```kotlin
AlertDialog(
    modifier = Modifier.animateEnterExit(
        enter = fadeIn() + scaleIn(initialScale = 0.95f),
        exit = fadeOut() + scaleOut(targetScale = 0.95f)
    ),
    // Content
)
```

#### f) Search Results Fade-In
```kotlin
LazyColumn {
    items(
        items = materials,
        key = { it.id }
    ) { material ->
        InventoryItemCard(
            material = material,
            modifier = Modifier.animateItemPlacement()
        )
    }
}
```

## Implementation Guidelines

### Content-First Card Design

```kotlin
@Composable
fun InventoryItemCard(
    material: ArtMaterial,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // PRIORITY 1: Material Name (large, bold)
            Text(
                text = material.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // PRIORITY 2: Stock Quantity (prominent)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${material.quantity} ${material.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // SECONDARY: Category Badge
            CategoryBadge(
                category = material.category,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}
```

### Pastel Color Application

```kotlin
// In your Theme.kt
val CraftNookColorScheme = lightColorScheme(
    primary = Color(0xFFD4A8E8),              // Soft lavender
    onPrimary = Color(0xFF3F2C47),            // Deep plum
    secondary = Color(0xFFB8E6D5),            // Mint green
    onSecondary = Color(0xFF2D4A40),          // Deep forest
    tertiary = Color(0xFFE8C4B0),             // Soft peach
    onTertiary = Color(0xFF4A3028),           // Deep brown
    surface = Color(0xFFFEFBFE),              // Nearly white
    surfaceVariant = Color(0xFFF5E6F5),       // Light lavender
    background = Color(0xFFFEFBFE),           // Nearly white
    errorContainer = Color(0xFFE8D4D4)        // Soft blush
)
```

### Micro-interaction Patterns

```kotlin
// 1. Search field with subtle focus animation
var isFocused by remember { mutableStateOf(false) }

OutlinedTextField(
    value = searchQuery,
    onValueChange = { updateSearchQuery(it) },
    modifier = Modifier
        .fillMaxWidth()
        .animateContentSize(),
    onFocusEvent = { focusState ->
        isFocused = focusState.isFocused
    },
    colors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )
)

// 2. Animated category filter selection
FilterChip(
    selected = selectedCategory == category,
    onClick = { selectCategory(category) },
    label = { Text(category) },
    modifier = Modifier.animateContentSize(),
    colors = FilterChipDefaults.filterChipColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        selectedContainerColor = MaterialTheme.colorScheme.secondary
    )
)

// 3. Delete button with confirmation animation
var showDeleteConfirm by remember { mutableStateOf(false) }

Button(
    onClick = { showDeleteConfirm = true },
    modifier = Modifier
        .size(36.dp)
        .animateContentSize(),
    shape = RoundedCornerShape(6.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.errorContainer
    )
) {
    Icon(Icons.Filled.Delete, contentDescription = "Delete")
}

if (showDeleteConfirm) {
    AlertDialog(
        modifier = Modifier.animateEnterExit(
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ),
        // Dialog content
    )
}
```

## Best Practices Summary

✅ **DO**:
- Prioritize material name and quantity in visual hierarchy
- Use pastel colors consistently throughout the app
- Add subtle animations to interactive elements
- Remove unnecessary data fields from primary views
- Test readability across different screen sizes
- Use Material 3 components as-is (they're designed well)

❌ **DON'T**:
- Display too much information on a single card
- Use bright, saturated colors that clash with pastels
- Add distracting animations (keep them subtle, < 300ms)
- Hide critical inventory information
- Over-customize Material 3 components
- Forget accessibility (ensure sufficient color contrast)

## Accessibility Considerations

- Ensure pastel colors meet WCAG AA contrast ratios
- Don't rely solely on color to convey information
- Include icon + text combinations for clarity
- Test with accessibility tools (Compose A11y)
- Maintain readable font sizes (minimum 12sp for body text)

## Resources

- Material Design 3 Guidelines: https://m3.material.io
- Jetpack Compose Animation: https://developer.android.com/jetpack/compose/animation
- Color Contrast Checker: https://webaim.org/resources/contrastchecker/
- Pastel Color Palettes: https://coolors.co (filter by pastel)
