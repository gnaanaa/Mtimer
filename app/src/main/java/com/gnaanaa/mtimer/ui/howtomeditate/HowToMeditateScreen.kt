package com.gnaanaa.mtimer.ui.howtomeditate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToMeditateScreen(
    onOpenDrawer: () -> Unit,
    onCreatePreset: (String, Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HOW TO MEDITATE", fontFamily = DotMatrix, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "STILL THE MIND, KNOW YOURSELF",
                fontFamily = DotMatrix,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Practical methods drawn from Sri M's teachings and the Nath tradition. Whether you're sitting for the first time or deepening a long practice — start here.",
                fontFamily = InterFont,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(24.dp))

            SectionTitle("BEFORE YOU BEGIN")
            
            BulletPoint("PLACE", "A clean, quiet spot. Same place each time helps the mind settle faster.")
            BulletPoint("POSTURE", "Spine upright, relaxed. Cross-legged on the floor or seated on a chair — both work. Hands rest on knees, palms facing up or down.")
            BulletPoint("TIME", "Early morning (brahma muhurta, roughly 4–6 AM) or dusk are traditionally considered ideal. Consistency matters more than duration.")
            BulletPoint("ATTITUDE", "No forcing. Let the practice unfold. Effort without tension is the key.")

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(24.dp))

            SectionTitle("METHODS")
            
            MethodAccordion(
                number = 1,
                title = "Breath Awareness (Prana Dhyan)",
                tradition = "Universal foundation",
                level = "Beginner",
                duration = "10 minutes (range: 5–20 min)",
                durationMins = 10,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "The most universal starting point. Sri M consistently recommends this as the gateway — simply observe the natural breath without controlling it. In the Nath tradition, awareness of breath (prana) is the first step in uniting the individual with the universal. The breath is always present, always available, and requires no special conditions.",
                        steps = listOf(
                            "Settle into posture — Sit comfortably with spine erect. Rest hands on knees, palms facing up or down. Close your eyes gently.",
                            "Natural breath — Don't change your breathing. Simply notice it — the slight cool touch at the nostrils on the in-breath, the warmth on the out-breath.",
                            "Name and release — If a thought arises, mentally note \"thinking\" and return to the breath. No frustration needed — this noticing is the practice itself.",
                            "Deepen the awareness — After a few minutes, let attention ride the breath into the body — feel the chest or belly rise and fall. Stay receptive.",
                            "Close — At the end, sit quietly for a minute. Bring awareness to the room before opening eyes."
                        ),
                        note = "Sri M often says: \"The breath is the bridge between the conscious and the unconscious.\" Even one minute of genuine breath awareness is better than ten minutes of restless sitting.",
                        variations = listOf(
                            "A. Body-scan breath awareness" to "After settling the breath, slowly move attention through the body in sync with each inhale and exhale — feet, legs, belly, chest, hands, shoulders, face. Each out-breath releases tension in that area. One full round takes 5–10 minutes. This is particularly useful for practitioners who carry physical tension and struggle to sit still.",
                            "B. Counting breaths" to "For a more anchored practice when the mind is very restless: count each exhale from 1 to 10, then restart. If you lose count, simply begin again at 1. This is a classic Zen adaptation of breath awareness and requires no belief system — just attention. Sri M endorses counting as a useful training wheel for beginners.",
                            "C. Touch-point awareness (Nath variation)" to "Instead of counting or labeling, bring all attention to a single point — the tip of the nostrils or the upper lip — and notice only the sensation of breath passing that point. No following the breath into the body. Just that one touch. This is close to the Nath concept of dharana (concentration) applied to prana, and it steadies the mind more forcefully than general breath observation.",
                            "D. Breath with visualisation (Sri M's green light method)" to "Sri M describes a vivid variation: as you inhale, visualise breathing in a beautiful, calming green light — like the colour of a paddy field — filling every part of the body. As you exhale, visualise all negativity and limitation leaving through the nostrils, carried away by the wind. After several rounds, rest in the inner stillness that remains. This method is especially effective when emotional heaviness or anxiety is present.",
                            "E. Kumbhaka — breath retention (intermediate)" to "Once the breath has settled naturally through regular practice, a gentle retention (kumbhaka) can be introduced. Inhale — pause — exhale. Sri M's instruction: hold the in-breath for roughly half a minute, without strain. The pause between breaths is, in both Sri M's and Nath teachings, a point of expanded stillness — a momentary gap where the usual mental movement quietens. Do not force; let the retention shorten or lengthen on its own."
                        )
                    )
                }
            )

            MethodAccordion(
                number = 2,
                title = "Hum-Sau Mantra Meditation",
                tradition = "Sri M (Kriya / Nath lineage)",
                level = "Beginner",
                duration = "20 minutes (range: 10–40 min)",
                durationMins = 20,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "Sri M's specific instruction from On Meditation: mentally chant Hum on the inhalation and Sau on the exhalation. This is the Hamsa mantra — the ajapa japa (mantra chanted without chanting) — which the scriptures say resonates in every living being's breath approximately 21,600 times a day. Becoming conscious of it is the practice. The meaning: Ham = I am; Sa/Sau = That (the universal). Together: \"I am That.\"\n\nThis form aligns with Paramahansa Yogananda's Hong-Sau technique from the same Kriya lineage that deeply influenced Sri M — approaching the mantra from the ego (Hum/I am) toward the universal (Sau/That), which Sri M and Yogananda both considered the natural direction for a practitioner beginning from ordinary consciousness.",
                        steps = listOf(
                            "Sit and settle — Comfortable posture, eyes closed. Take three slow, deliberate breaths to signal the start of the practice.",
                            "Hear 'Hum' on the in-breath — As you breathe in, mentally hear the sound \"Hum\" (like a soft humming). Don't force — let it arise as you inhale.",
                            "Hear 'Sau' on the out-breath — As you breathe out, mentally hear \"Sau\" (rhymes with \"saw\"). There is no chanting aloud.",
                            "Let it become natural — After a few rounds, stop \"doing\" the mantra — just listen for it within the breath. Let it run on its own.",
                            "Rest in the gap — Notice the small pause between breaths. Sri M describes this as a momentary touch of stillness — don't grasp it, just notice."
                        ),
                        note = "Sri M's instruction is clear: \"Breathe in, mentally chant Hum, hold it for half a minute, don't stress it, and exhale... mentally chant Sau and allow it to go.\" If the mind wanders mid-mantra, simply pick it up at the next breath. There is no penalty. Hum-Sau naturally calms mental chatter because it occupies the same rhythm as breathing."
                    )
                }
            )

            MethodAccordion(
                number = 3,
                title = "Trataka — Steady Gazing",
                tradition = "Nath tradition (shatkarma)",
                level = "Beginner",
                duration = "10 minutes (range: 5–20 min)",
                durationMins = 10,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "One of the six shatkarmas (purificatory practices) in the Nath tradition, and a direct method for developing one-pointed concentration. By holding the gaze on a single point, the restless nature of the mind is gradually stilled. Outer trataka develops into inner trataka as the practice matures.",
                        steps = listOf(
                            "Set up — Place a candle flame at eye level, about an arm's length away. Alternatively, a small dark dot on white paper works. Dim the room.",
                            "Open-eyed gazing — Look at the flame (or point) without blinking for as long as comfortable. Don't strain — let tears come if they do.",
                            "Close and internalize — When eyes tire, close them and hold the after-image in the mind's eye. Keep attention steady on this inner image.",
                            "Alternate — Open eyes when the image fades. Gaze again. Repeat this cycle several times in one session.",
                            "Transition to stillness — After the final cycle, close eyes and simply rest in stillness — no object, no image. Just awareness."
                        ),
                        note = "The Nath tradition holds that trataka purifies the eyes and develops the capacity for inner vision (antara drishti). Begin with 5-minute sessions to avoid eye strain and increase gradually."
                    )
                }
            )

            MethodAccordion(
                number = 4,
                title = "Ajapa-Japa — The Unchanted Mantra",
                tradition = "Nath tradition",
                level = "Intermediate",
                duration = "30 minutes (range: 15–45 min)",
                durationMins = 30,
                onCreatePreset = onCreatePreset,
                content = {
                    Column {
                        Text(
                            "WHAT IT IS",
                            fontFamily = DotMatrix,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "A cornerstone practice of the Nath tradition. Ajapa means \"that which is not chanted\" — the Hamsa/Soham mantra flows continuously with the breath without any effort to repeat it. Unlike Hum-Sau, attention here moves with the breath along a specific inner pathway through the body, making it both a breath practice and a subtle-body practice.\n\n" +
                            "A note on the syllable assignment — two authentic versions exist:\n" +
                            "The ancient texts are genuinely divided on this, and this is not an error but a documented scriptural divergence:\n\n" +
                            "• The Gheranda Samhita, Shiva Samhita, and most Yoga-Upanishads (the foundational Nath hatha yoga texts) teach: So/Sa on the inhale, Ham on the exhale — producing the So-Ham mantra (\"I am That\").\n" +
                            "• The Vijnana Bhairava Tantra specifically states the opposite: Ham on the inhale, Sa on the exhale — producing the Ham-Sa (Hamsa) mantra (\"That I am\").\n\n" +
                            "Both forms carry the same meaning and are considered equally valid. The difference is one of starting point: So-Ham begins with \"I\" moving toward \"That\"; Hamsa begins with \"That\" entering as \"I.\" Sri M's Hum-Sau teaching (method 2) follows this second form. For Ajapa-Japa as a Nath body-pathway practice, the more widely taught classical form is So on inhale, Ham on exhale, and that is what the steps below follow. If you have been initiated into a specific lineage, follow your teacher's instruction above all else.",
                            fontFamily = DotMatrix,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            "STEP BY STEP",
                            fontFamily = DotMatrix,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val steps = listOf(
                            "Ground in breath awareness first — Spend five minutes in simple breath awareness before entering this practice.",
                            "Locate the pathway — In Nath practice, the breath is understood to travel from the navel centre (or lower belly) upward on the in-breath, and descend from the throat downward on the out-breath. Feel this movement.",
                            "'So' on the inhale — As the breath enters, mentally hear \"So\" while feeling the breath rise from the lower belly toward the head.",
                            "'Ham' on the exhale — As the breath leaves, mentally hear \"Ham\" as it descends from head toward the belly. Together: So-Ham — \"I am That.\"",
                            "Let it run spontaneously — The goal is not deliberate repetition but listening — becoming aware of a mantra already happening. Eventually, awareness rests as the witness of the breath-mantra."
                        )
                        steps.forEachIndexed { index, step ->
                            Text(
                                "${index + 1}. $step",
                                fontFamily = DotMatrix,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "The Nath texts say this mantra repeats 21,600 times daily in every living being — the practice is simply becoming conscious of it. The textual divergence on syllable assignment has been debated for centuries; what matters far more than which version you choose is that you settle on one form and stay with it. Switching between versions mid-practice defeats the purpose. Advanced practitioners extend awareness of the pathway into the spinal channel (sushumna).",
                                modifier = Modifier.padding(12.dp),
                                fontFamily = DotMatrix,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            )

            MethodAccordion(
                number = 5,
                title = "Self-Enquiry (Atma Vichara)",
                tradition = "Sri M (Ramana Maharshi lineage)",
                level = "Intermediate",
                duration = "30 minutes (range: 15–60 min)",
                durationMins = 30,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "Sri M was deeply influenced by Ramana Maharshi, whose primary method was the inquiry \"Who am I?\" This is not a philosophical question — it is a practice. The question is turned back on itself to dissolve the questioner. Each thought, when traced back to its source, leads to the \"I\" sense — and following that \"I\" inward is the practice.",
                        steps = listOf(
                            "Sit quietly, eyes closed — Begin with 5 minutes of breath awareness to calm the surface of the mind.",
                            "Ask \"Who am I?\" — Not aloud, not with the intellect — pose this as an inward attention. Direct the mind's focus toward its own source.",
                            "Follow the 'I' back — When a thought arises, ask: who is thinking this? Each thought points back to the thinker. Keep following it inward.",
                            "Rest in the question — Don't expect a verbal answer. The question itself is the practice. If the mind wanders, gently re-ask. Each return deepens it.",
                            "Sit in the aftermath — After 15–20 minutes, release the inquiry. Sit in whatever quality of stillness or openness has emerged."
                        ),
                        note = "Sri M emphasises: \"This is not for beginners to rush into. Establish a stable breath-awareness or Hum-Sau practice first. The Self-enquiry becomes effective when the mind has thinned.\" Give it months of preparation before expecting depth."
                    )
                }
            )

            MethodAccordion(
                number = 6,
                title = "Nada Yoga — Listening to Inner Sound",
                tradition = "Nath tradition",
                level = "Intermediate",
                duration = "20 minutes (range: 10–40 min)",
                durationMins = 20,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "The Nath tradition speaks of Anahata Nada — the unstruck sound that resonates within. This practice involves withdrawing attention from outer sounds to discover subtler internal resonances. It is one of the most direct paths in Nath yoga for pratyahara (sense withdrawal).",
                        steps = listOf(
                            "Block outer sound — Sit with eyes closed. Gently press earflaps (tragus) with thumbs or use soft earplugs. Hearing naturally turns inward.",
                            "Listen neutrally — Do not look for any particular sound. Just listen to whatever is present inwardly. You may notice a ringing, humming, buzzing, or rushing sound.",
                            "Choose the subtlest — Over time, you may notice layers of sound — gross to subtle. Nath texts recommend attending to the most subtle sound you can perceive, and staying with it.",
                            "Merge attention into the sound — Let awareness become the listening — not a person listening to a sound, but pure listening itself.",
                            "Release and rest — After the session, release the ears and sit in open awareness."
                        ),
                        note = "The Hatha Yoga Pradipika (core Nath text) names 10 inner sounds from gross to subtle. Don't worry about identifying them. Simply listen with full presence — the practice itself is transformative."
                    )
                }
            )

            MethodAccordion(
                number = 7,
                title = "Inner Gazing at Ajna (Third Eye)",
                tradition = "Nath tradition",
                level = "Advanced",
                duration = "20 minutes (range: 10–30 min)",
                durationMins = 20,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "An advanced extension of trataka. After establishing steady concentration, attention is directed to the Ajna chakra — the point between and slightly above the eyebrows. In Nath tradition, this is the seat of the guru principle and the meeting point of the three main nadis (ida, pingala, sushumna).\n\nPre-requisite: At least 2–3 months of regular trataka practice. The power of concentration must be established first.",
                        steps = listOf(
                            "Sit, eyes closed — After a settling breath practice, gently turn the eyes upward and inward toward the point between the eyebrows. This is a soft, internal gaze — no straining.",
                            "Hold without force — Any strain creates headache. The gaze should feel like you are simply \"looking\" toward that area from within. Hold for as long as comfortable.",
                            "Observe inner phenomena — Some practitioners see colours, lights, or forms. Treat all appearances as objects of awareness — don't chase or analyse them.",
                            "Withdraw and rest — Release the upward gaze. Rest in simple awareness. The integration after the practice is as important as the practice itself."
                        ),
                        note = "Sri M cautions against forcing chakra practices: \"These things open naturally as the practice matures and the mind purifies.\" Force creates imbalance; regular practice create readiness."
                    )
                }
            )

            Spacer(Modifier.height(32.dp))
            SectionTitle("UNIVERSAL PRINCIPLES")
            
            PrincipleItem("Witness without judging.", "Thoughts will arise. Notice them, let them go. You are not the thought; you are the one observing it.")
            PrincipleItem("Return, don't restart.", "Every time you notice you've drifted, gently come back. That moment of return is the practice, not a failure.")
            PrincipleItem("Regularity over intensity.", "Twenty quiet minutes each day builds more than two hours once a week.")
            PrincipleItem("The breath is always there.", "Whatever method you use, the breath is a reliable anchor when the mind gets restless.")
            PrincipleItem("Expect nothing, receive everything.", "Meditation is not a performance. The sessions where \"nothing seems to happen\" are often doing the deepest work.")

            Spacer(Modifier.height(32.dp))
            SectionTitle("A NOTE ON LINEAGE")
            Text(
                "The methods here draw from two interconnected streams:\n\n" +
                "Sri M (Mumtaz Ali Khan) is a contemporary teacher in the Nath tradition, initiated by the mahayogi Maheshwarnath Babaji in the Himalayas. His teachings blend the non-dual Vedanta of Ramana Maharshi with Kriya yoga and classical Nath practice. His book On Meditation and his autobiography Apprenticed to a Himalayan Master are foundational texts for practitioners seeking to understand the background of these methods.\n\n" +
                "The Nath tradition is one of India's oldest living yogic lineages, associated with Matsyendranath and Gorakhnath. Its canonical texts — the Hatha Yoga Pradipika, Gheranda Samhita, and Shiva Samhita — systematise breathwork, sound meditation, concentration, and inner body awareness into a complete path.\n\n" +
                "Both streams agree on one thing: the real teaching is in the practice itself. Read as much as you like, but sit down and breathe.",
                fontFamily = InterFont,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(32.dp))
            Text(
                "\"The breath is the bridge between the conscious and the unconscious.\"\n— Sri M",
                fontFamily = DotMatrix,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = DotMatrix,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 4.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun BulletPoint(label: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            label,
            fontFamily = DotMatrix,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            content,
            fontFamily = InterFont,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun MethodAccordion(
    number: Int,
    title: String,
    tradition: String,
    level: String,
    duration: String,
    durationMins: Int,
    onCreatePreset: (String, Int) -> Unit,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dotColor = when (level.lowercase()) {
        "beginner" -> Color(0xFF4CAF50)
        "intermediate" -> Color(0xFFFF9800)
        "advanced" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (expanded) 0.4f else 0.2f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "$number. ${title.uppercase()}",
                    fontFamily = DotMatrix,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Text(
                        "Tradition: $tradition",
                        fontFamily = InterFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Level: $level",
                        fontFamily = InterFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Duration: $duration",
                        fontFamily = InterFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    content()
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { onCreatePreset(title, durationMins) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(
                            "CREATE ${title.split("—")[0].trim().uppercase()} PRESET",
                            fontFamily = DotMatrix,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MethodContent(
    description: String,
    steps: List<String>,
    note: String,
    variations: List<Pair<String, String>> = emptyList()
) {
    Column {
        Text(
            "WHAT IT IS",
            fontFamily = DotMatrix,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            description,
            fontFamily = InterFont,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            "STEP BY STEP",
            fontFamily = DotMatrix,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        steps.forEachIndexed { index, step ->
            Text(
                "${index + 1}. $step",
                fontFamily = InterFont,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                note,
                modifier = Modifier.padding(12.dp),
                fontFamily = InterFont,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        if (variations.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "VARIATIONS TO EXPLORE",
                fontFamily = DotMatrix,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            variations.forEach { (vTitle, vDesc) ->
                Spacer(Modifier.height(4.dp))
                Text(
                    vTitle,
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    vDesc,
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun PrincipleItem(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            title,
            fontFamily = DotMatrix,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            content,
            fontFamily = InterFont,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
