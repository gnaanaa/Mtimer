package com.gnaanaa.mtimer.ui.howtomeditate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont
import com.gnaanaa.mtimer.ui.home.styleDottedDigits
import com.gnaanaa.mtimer.ui.theme.Spacing
import com.gnaanaa.mtimer.ui.theme.Radius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToMeditateScreen(
    onOpenDrawer: () -> Unit,
    onCreatePreset: (String, Int) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("HOW TO MEDITATE", fontFamily = DotMatrix, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.primary) },
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
                .padding(horizontal = Spacing.medium)
        ) {
            Spacer(Modifier.height(Spacing.tiny))
            Text(
                "A PRACTICAL GUIDE FOR EVERY LEVEL",
                fontFamily = InterFont,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            )
            Spacer(Modifier.height(Spacing.tiny))
            Text(
                "These are time-tested methods drawn from living yogic traditions in India — some over a thousand years old. They have been adapted here for clarity and accessibility, without diluting their depth. No prior belief, background, or flexibility required.",
                fontFamily = InterFont,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )

            Spacer(Modifier.height(Spacing.large))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(Spacing.large))

            SectionTitle("BEFORE YOU BEGIN")
            
            BulletPoint("PLACE", "A clean, quiet corner — indoors is fine. Using the same spot each time helps the mind recognise the signal and settle faster. It doesn't need to be special; it just needs to be consistent.")
            BulletPoint("POSTURE", "Spine upright, body relaxed. Sitting on a chair with feet flat on the floor works just as well as sitting cross-legged. What matters is that your back can be straight without being rigid, and that you won't need to shift around. If lying down, you'll likely fall asleep — save that for rest, not practice.")
            BulletPoint("TIME", "Early morning — before the day's noise begins — and early evening are both good windows. The world is quieter, and so are you. That said, the best time is whichever time you'll actually keep. Consistency across weeks matters far more than the hour on the clock.")
            BulletPoint("DURATION", "Start shorter than you think you need. Ten focused minutes beats forty restless ones. Build gradually — the capacity to sit deepens on its own with regular practice.")
            BulletPoint("ATTITUDE", "This is not a performance and there is no grade. Nothing special needs to happen. The instruction is simply to sit, pay attention, and return when you've drifted. That's the whole practice.")

            Spacer(Modifier.height(Spacing.large))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(Spacing.large))

            SectionTitle("METHODS")
            
            MethodAccordion(
                number = 1,
                title = "Watching the Breath",
                alsoKnownAs = "Breath awareness, Prana Dhyan",
                level = "Beginner",
                duration = "10 min (range: 5–20 min)",
                durationMins = 10,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "The breath is always happening. You don't have to create it, sustain it, or control it — it runs on its own. That makes it the most reliable object of attention available to you, at any moment, without any equipment or setup.\n\nThis practice asks only one thing: notice the breath without interfering with it. What sounds simple is in fact quite deep — because the moment you pay close attention to the breath, the mind tends to quiet down on its own. You're not forcing stillness; you're just watching something that's already there.\n\nThis is the recommended starting point for anyone new to meditation, and a practice that experienced meditators return to throughout their lives.",
                        steps = listOf(
                            "Settle — Sit comfortably, spine upright. Rest your hands on your knees or in your lap. Close your eyes gently.",
                            "Do nothing with the breath — Don't slow it, deepen it, or change it in any way. Just let it breathe itself. Notice what's actually there — the slight coolness at the nostrils on the inhale, the faint warmth on the exhale.",
                            "When a thought appears — Mentally note \"thinking\" — quietly, without judgment — and return your attention to the next breath. You'll do this many times per session. That's not failure; that returning is the practice.",
                            "Widen if you like — After a few minutes, let your attention move with the breath into the body. Feel the chest or belly rise and fall. Stay receptive rather than analytical.",
                            "Close gently — When the session ends, don't jump up. Sit quietly for a minute. Let the room come back to you before opening your eyes."
                        ),
                        note = "The breath sits right on the border between what we can control and what happens on its own — which is exactly why watching it is so useful. It connects the conscious and the automatic. Even one minute of genuine breath awareness, without doing anything else, is more valuable than ten minutes of unfocused sitting.",
                        variations = listOf(
                            "A. Body-scan with breath" to "After settling the breath, slowly move your attention through the body — feet, legs, belly, chest, hands, shoulders, face — staying with each area for a few breaths. Let each exhale release any tension you find there. One full round takes 5–10 minutes. Particularly useful if you carry a lot of physical tension and find it hard to sit still.",
                            "B. Counting breaths" to "When the mind is very restless, give it a small job: count each exhale from 1 to 10, then start again. If you lose count, simply restart at 1 — no self-criticism, just back to 1. This technique appears across multiple traditions and works because it gives the analytical mind just enough to do without taking over. Use it when you need it; set it aside when the breath alone is sufficient.",
                            "C. Single-point touch awareness" to "Instead of following the breath through the body, narrow your attention to one precise point — the tip of the nostrils or the upper lip — and notice only the sensation of air passing that spot. Nothing else. This trains concentration more sharply than general breath observation and is a good stepping stone toward the mantra practices below.",
                            "D. Breathing in light" to "As you inhale, visualise drawing in a calm, clear light — any colour that feels settling (green, gold, white). Let it fill the body completely. As you exhale, let go of whatever feels heavy — tension, worry, fatigue — carried out on the breath. After several rounds, rest in whatever stillness remains. This variation is especially effective when anxiety or emotional weight is present.",
                            "E. Breath with a pause (intermediate)" to "Once the breath has settled naturally across a few weeks of practice, you can gently introduce a pause after the inhale: breathe in, hold briefly without strain, breathe out. Don't count seconds — let the pause be as long as it is comfortable, nothing more. The stillness between breaths is, across many traditions, considered a window of unusual quiet. You're not manufacturing that quiet; you're simply noticing the gap that's already there."
                        )
                    )
                }
            )

            MethodAccordion(
                number = 2,
                title = "Mantra with the Breath (Hum-Sau)",
                alsoKnownAs = "Breath mantra, the unchanted mantra, Ajapa Japa",
                level = "Beginner",
                duration = "20 min (range: 10–40 min)",
                durationMins = 20,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "A mantra is a sound used as an object of attention in meditation. This practice pairs two sounds with the natural rhythm of breathing — one on the inhale, one on the exhale — so that the mantra and the breath become one movement. The mind has less room to wander when it's riding something that never stops.\n\nThe sounds used here are Hum (inhale) and Sau (exhale). These are not invented — they reflect the subtle sound the breath makes as it moves, something the ancient yogic texts describe as already happening in every living being roughly 21,600 times a day. The practice isn't creating the mantra; it's becoming aware of something already there.\n\nHum-Sau is one form of a family of breath mantras that includes the well-known So-Ham (\"I am That\"). Both point in the same direction — toward the recognition that what you are at the deepest level and what the universe is at its deepest level are not separate things. You don't need to hold or accept that idea for the practice to work. The sound and the breath do the work on their own.",
                        steps = listOf(
                            "Sit and settle — Comfortable posture, eyes closed. Take three natural breaths to mark the beginning of the practice.",
                            "Inhale with \"Hum\" — As you breathe in, mentally hear the sound \"Hum\" — soft, like a quiet humming. Don't force it; let it arise with the breath.",
                            "Exhale with \"Sau\" — As you breathe out, hear \"Sau\" (rhymes with \"saw\") accompanying the breath out. Still silent — no sound aloud.",
                            "Let go of doing it — After several rounds, stop actively placing the mantra. Just listen for it within the breath. Let it run on its own rhythm.",
                            "Notice the pause — There's a small natural gap between the exhale ending and the next inhale beginning. Don't try to extend it — just notice it. It tends to be unusually quiet."
                        ),
                        note = "If the mind wanders and you lose the mantra, pick it up at the next breath. There is no penalty, no falling behind. Hum-Sau works partly because it occupies the same rhythm as breathing — the mind has less room to wander when attention is riding something that never stops."
                    )
                }
            )

            MethodAccordion(
                number = 3,
                title = "Steady Gazing",
                alsoKnownAs = "Fixed-point gazing, candle meditation, Trataka",
                level = "Beginner",
                duration = "10 min (range: 5–20 min)",
                durationMins = 10,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "The mind tends to follow the eyes. When the eyes are completely still, the mind has less to chase. This practice uses that connection deliberately — holding a steady, relaxed gaze on a single object trains the attention to stay in one place without forcing it.\n\nOver time, the practice moves inward: after gazing at the external flame or point, you close your eyes and hold the image internally. The transition from outer object to inner image is the beginning of turning attention on itself — which is where all deeper meditation practices eventually go.\n\nThis is a particularly useful method for people who find breath-based practices too abstract. Having something concrete to look at gives the mind a clear, manageable task.",
                        steps = listOf(
                            "Set up — Place a lit candle at eye level, roughly an arm's length away. A small dark circle on white paper works equally well. Dim the room if possible.",
                            "Gaze without forcing — Look at the flame (or point) with a relaxed, steady gaze. Try not to blink, but don't strain. If tears come, let them — that's a normal response. When eyes water or tire, that's the signal to close.",
                            "Close and hold the image — When you close your eyes, an after-image of the flame will appear in the mind's eye. Keep your attention on it. Let it be whatever colour or shape it naturally takes.",
                            "Cycle — When the image fades, open your eyes and gaze at the flame again. Repeat this several times through the session.",
                            "Rest in stillness — For the final cycle, close your eyes and release the image entirely. Simply sit in whatever quality of stillness or openness is present. Don't look for anything."
                        ),
                        note = "Begin with 5 minutes and increase gradually over weeks to avoid eye strain. The practice becomes noticeably more effective with repetition — what feels effortful at first becomes quite natural after a few weeks of daily sitting."
                    )
                }
            )

            MethodAccordion(
                number = 4,
                title = "Breath Mantra with Inner Pathway (So-Ham)",
                alsoKnownAs = "The spontaneous mantra with body awareness, Ajapa Japa",
                level = "Intermediate",
                duration = "30 min (range: 15–45 min)",
                durationMins = 30,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "This builds on the breath mantra practice (method 2). The sounds here are So (inhale) and Ham (exhale). But rather than simply pairing sound with breath, attention now moves with the breath along a felt pathway through the body — rising from the lower belly toward the head on the inhale, descending from the head back toward the belly on the exhale.\n\nThis makes it both a breath practice and an inner body practice. You're training the mind to follow movement inside, which naturally deepens concentration and builds toward the more interior practices that follow.\n\nA note on the sounds: The ancient texts give differing instructions on which syllable goes with which breath, and both versions are considered valid within their respective sources. The form used here — So on the inhale, Ham on the exhale — follows the most widely taught classical texts on this practice. If you have been taught a different form by a teacher, follow your teacher. What matters most is choosing one form and staying with it. Switching creates confusion, not depth.",
                        steps = listOf(
                            "Ground first — Begin with 5 minutes of simple breath awareness before entering this practice. Let the surface of the mind settle.",
                            "Feel the pathway — Notice that the breath seems to move through the body: something rises on the inhale, something descends on the exhale. You don't need to visualise this anatomically — just feel the direction of movement.",
                            "\"So\" rises with the inhale — As the breath comes in, mentally hear \"So\" while sensing the breath rise from the lower belly toward the head.",
                            "\"Ham\" descends with the exhale — As the breath goes out, hear \"Ham\" while sensing the movement descend from the head back toward the belly.",
                            "Let it run itself — As with Hum-Sau, the goal is not to keep actively repeating — it's to listen for something already moving. Eventually the attention simply rides the breath-mantra without effort, as a quiet witness."
                        ),
                        note = "This practice is most effective after a few months of regular breath awareness or Hum-Sau. The \"pathway\" aspect may feel conceptual at first — that's fine. Keep practicing and the felt sense of it tends to emerge on its own without being constructed."
                    )
                }
            )

            MethodAccordion(
                number = 5,
                title = "Self-Inquiry — Who Is Thinking?",
                alsoKnownAs = "The question that dissolves itself, Atma Vichara",
                level = "Intermediate",
                duration = "30 min (range: 15–60 min)",
                durationMins = 30,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "Every thought has a thinker. Every experience has someone who is experiencing it. This practice asks: who is that?\n\nNot as a philosophical puzzle — as a direct investigation. Rather than following thoughts outward into their content, you turn attention back toward where thoughts come from. You're looking for the \"I\" that is behind every experience — and the looking itself is the practice.\n\nThe method is deceptively simple: when a thought arises, ask \"who is thinking this?\" The question points back to the source. Follow that pointer inward. You're not looking for a verbal answer — there isn't one. The question is a tool that gradually quiets the usual mental activity by redirecting it toward its own root.\n\nThis is not suitable as a first practice. It requires a mind that is already somewhat settled — not because it's complicated, but because an unsettled mind will simply generate more thoughts in response to the question. Build a stable foundation in breath awareness or mantra practice first, then come to this.",
                        steps = listOf(
                            "Settle the mind first — Begin with at least 5–10 minutes of breath awareness. The inquiry goes nowhere on a turbulent mind.",
                            "Ask the question inwardly — Not aloud, not analytically. Pose \"Who am I?\" or more practically \"Who is thinking this?\" as a direction of attention, not a question awaiting a verbal answer.",
                            "Follow each thought back — When a thought arises, don't follow its content. Ask instead: who is having this thought? Each thought points back to a thinker. Trace that pointer inward.",
                            "Rest when the question dissolves — At some point the question may seem to disappear — not because you've found an answer, but because the questioner has become quiet. Rest there. This is the fruit of the practice.",
                            "Release and sit — After 20–30 minutes, let go of the inquiry. Sit in whatever openness or stillness has gathered. Don't analyse it; just be in it."
                        ),
                        note = "This practice becomes genuinely effective when the mind has been quieted by months of simpler practice. It's not about thinking harder — it's about the question outlasting the usual mental activity. Build a stable foundation in breath awareness or mantra practice first. Then come to this."
                    )
                }
            )

            MethodAccordion(
                number = 6,
                title = "Listening Inward",
                alsoKnownAs = "Inner sound meditation, Nada Yoga",
                level = "Intermediate",
                duration = "20 min (range: 10–40 min)",
                durationMins = 20,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "There are sounds present inside the body and nervous system that we almost never hear — not because they're faint, but because the outer world is louder and our attention points outward. This practice reverses that: you gently close off outer hearing and simply listen to what's within.\n\nWhat most people discover, once external sound is reduced, is a background resonance — a faint ringing, humming, buzzing, or rushing tone that is almost always present once you turn toward it. This is recognised across many contemplative traditions as a genuinely useful object of meditation: attending to increasingly subtle layers of inner sound is one of the most natural routes to deep, effortless inner absorption.\n\nNo special sensitivity or musical hearing is required. Anyone can do this.",
                        steps = listOf(
                            "Seal the ears gently — Sit with eyes closed. Press the small flap of cartilage at the entrance of each ear (the tragus) inward with your thumbs, or use soft foam earplugs. Outer sounds recede; inner sounds become audible.",
                            "Simply listen — Don't look for anything specific. Just listen openly to whatever is there. You may notice a tone, a hum, a ringing — perhaps several sounds layered.",
                            "Move toward the subtler — If you notice several sounds, gently let your attention settle on the most subtle one you can hear. Don't strain for it — stay receptive. The subtler the sound you can track, the deeper the absorption tends to go.",
                            "Become the listening — Rather than \"a person listening to a sound,\" let the boundary soften. Pure listening, with no separate listener. If that's too abstract, just stay with the sound. The absorption happens on its own.",
                            "Release and sit open — At the end of the session, unplug the ears. Sit for a minute in open awareness before moving. Notice how ordinary sounds seem different on the other side of this practice."
                        ),
                        note = "Classical texts on this practice describe many layers of inner sound, from coarse to extremely subtle. Don't worry about identifying or progressing through them. Simply listen with full presence. This practice rewards patience and consistency more than any particular technique."
                    )
                }
            )

            MethodAccordion(
                number = 7,
                title = "Inner Concentration (Advanced)",
                alsoKnownAs = "Inner gazing, third-eye concentration, Ajna meditation",
                level = "Advanced",
                duration = "20 min (range: 10–30 min)",
                durationMins = 20,
                onCreatePreset = onCreatePreset,
                content = {
                    MethodContent(
                        description = "Once steady gazing has built genuine one-pointed concentration — where attention can hold on a single object without constant slipping — that same capacity can be turned fully inward, with no external object at all.\n\nThe \"object\" in this practice is the point between and slightly above the eyebrows — a location that, once you can hold attention steadily, functions as a natural gathering point for concentrated awareness. With eyes closed, the gaze is turned softly toward this inner point. You're not looking at anything visible; you're pointing attention toward a location within and holding it there with a relaxed, steady focus.\n\nWhat happens next varies: some people notice subtle light or colour, some feel a sense of pressure, some simply experience deepening quiet. Whatever arises, the instruction is the same — observe it without chasing it. Don't jump to this practice prematurely. Straining before the concentration is built produces headaches, not depth.",
                        steps = listOf(
                            "Begin with breath settling — Spend 5–10 minutes in simple breath awareness before turning inward. Let ordinary mental noise settle first.",
                            "Turn the gaze inward and upward — With eyes closed, gently direct the eyes upward and inward, toward the midpoint between the eyebrows. This should feel like a natural soft look, not a strain — like resting your gaze on a distant horizon.",
                            "Hold without gripping — Light and steady is the quality to aim for. If you feel strain or the beginning of a headache, ease off immediately. This is a sign of too much effort, not too little.",
                            "Observe without chasing — Lights, colours, or subtle imagery may appear. Don't pursue them or try to make them sharper. Simply observe whatever arises, and return to the inner point when attention drifts.",
                            "Release and integrate — After the session, let the inner gaze go completely. Sit in open, ordinary awareness. The period immediately after — when you're doing nothing in particular — is often when the deeper effects are felt most clearly. Don't rush past it."
                        ),
                        note = "Force produces nothing here. The capacity for this practice opens gradually through regular, patient work with simpler methods. Think of it as the natural destination of a progression that begins with watching the breath. Each method prepares the ground for the next. Steadiness built through months of earlier practice is what makes this one work — not willpower applied directly to it."
                    )
                }
            )

            Spacer(Modifier.height(32.dp))
            SectionTitle("PRINCIPLES THAT APPLY TO EVERY METHOD")
            Text(
                "Regardless of which practice you sit with, these hold.",
                fontFamily = InterFont,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            PrincipleItem("You are not your thoughts.", "Thoughts will arise during meditation — always. That's not a problem to solve. The practice is to notice them without being swept away. You are the one watching the thoughts, not the thoughts themselves. This distinction, felt even once, changes things.")
            PrincipleItem("Every return is the practice.", "The moment you notice you've drifted and come back — to the breath, the mantra, the question — that moment of return is the meditation. It's not an interruption of the practice; it is the practice. Each return builds the capacity to return faster next time.")
            PrincipleItem("Regularity beats intensity.", "Twenty quiet minutes each day will build more than two hours once a week. The mind changes through repetition over time, not through occasional long sessions. Show up consistently. Depth comes on its own.")
            PrincipleItem("The breath is always your anchor.", "Whatever method you're using, if you get lost or overwhelmed, return to simply watching the breath. It's always available, always works, and requires nothing from you except attention.")
            PrincipleItem("Nothing happening is still something happening.", "Sessions where it feels like \"nothing is going on\" are not wasted. The quietening of the mind is often invisible from the inside. The effects of regular practice tend to show up in daily life — slightly more patience, a bit less reactivity, a little more space between stimulus and response — before they show up dramatically in the session itself.")

            Spacer(Modifier.height(32.dp))
            SectionTitle("WHERE THESE METHODS COME FROM")
            Text(
                "The practices in this guide are drawn primarily from two interconnected streams: the teachings of Sri M (a contemporary teacher in the living Nath yogic lineage, and author of On Meditation and Apprenticed to a Himalayan Master), and the classical texts of the Nath tradition — among them the Hatha Yoga Pradipika, Gheranda Samhita, and Vingnana Bhairava Tantra. These methods are over a thousand years old in origin and have been transmitted through unbroken teacher lineages to the present day.\n\n" +
                "These methods belong to no single religion and require no particular belief to practise. They have been used by people of every background. What they require is regular practice, some patience, and genuine curiosity about what the mind actually is.\n\n" +
                "Where the classical sources themselves disagree — as they do, for instance, on the precise syllable assignment of certain breath mantras — this guide notes the disagreement plainly rather than papering over it. The uncertainty is real and has been debated for centuries. It doesn't prevent the practice from working.\n\n" +
                "Read what's useful. Set aside what isn't. Then sit down and breathe.",
                fontFamily = InterFont,
                fontSize = 14.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(32.dp))
            Text(
                "\"The breath sits right at the border between what we can control and what happens on its own. That's exactly why watching it is so useful.\"",
                fontFamily = InterFont,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
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
        fontFamily = InterFont,
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun BulletPoint(label: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            label,
            fontFamily = InterFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = content.styleDottedDigits(),
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
    alsoKnownAs: String,
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

    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.micro),
        shape = RoundedCornerShape(Radius.medium),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (expanded) 0.4f else 0.2f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(Spacing.tiny)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(Modifier.width(Spacing.small))
                Text(
                    text = "$number. ${title.uppercase()}".styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
            
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = Spacing.medium, end = Spacing.medium, bottom = Spacing.medium)) {
                    Text(
                        "Also known as: $alsoKnownAs",
                        fontFamily = InterFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        "Level: $level",
                        fontFamily = InterFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Recommended duration: $duration".styleDottedDigits(),
                        fontFamily = InterFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(Spacing.small))
                    content()
                    Spacer(Modifier.height(Spacing.medium))
                    val accentColor = if (isDark) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

                    Button(
                        onClick = { onCreatePreset(title, durationMins) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Radius.small),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor.copy(alpha = if (isDark) 0.15f else 0.8f),
                            contentColor = if (isDark) accentColor else MaterialTheme.colorScheme.onPrimary
                        ),
                        border = if (isDark) BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)) else null
                    ) {
                        Text(
                            text = "CREATE ${title.split("(")[0].trim().uppercase()} PRESET".styleDottedDigits(),
                            fontFamily = InterFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
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
            fontFamily = InterFont,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description.styleDottedDigits(),
            fontFamily = InterFont,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Text(
            "STEP BY STEP",
            fontFamily = InterFont,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        )
        steps.forEachIndexed { index, step ->
            Text(
                text = "${index + 1}. $step".styleDottedDigits(),
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
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "GUIDANCE NOTE",
                    fontFamily = InterFont,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = note.styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        if (variations.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                "VARIATIONS TO EXPLORE",
                fontFamily = InterFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
            variations.forEach { (vTitle, vDesc) ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = vTitle.uppercase().styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = vDesc.styleDottedDigits(),
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
            text = title.styleDottedDigits(),
            fontFamily = InterFont,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = content.styleDottedDigits(),
            fontFamily = InterFont,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
