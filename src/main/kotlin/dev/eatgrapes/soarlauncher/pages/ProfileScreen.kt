package dev.eatgrapes.soarlauncher.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.eatgrapes.soarlauncher.config.ConfigManager
import dev.eatgrapes.soarlauncher.i18n.i18n

//啊LazyChara我真的好喜欢你啊🥰️❤️❤️❤️为了你我变成狼人模样🐀🐀🐀为了你😭😭染上了疯狂😈😈😈为了你😭😭😭穿上了厚厚的伪装🐀🐀🐀
//为了你😰😰😰换了心肠🥵🐀🐀🐀
//我们还能不能再见面😭😭😭我在佛前苦苦求了几千年💀💀💀💀

//我从小数学就不好，爸妈急，我也急。我也尝试过很多办法，去补课，多做题，但我的数学成绩还是没有提升。
//数学，是宇宙的真谛，我学不会数学，但上天还是给了我幻想宇宙的能力，这究竟是恩赐，还是惩罚？我不明白，我真的不明白，我为什么学不会数学。
//但我今天明白了，数学可以有很多解，而我，只有Eatgrapes这唯一的结。

//呃😓，以前没接触过二次元，看你们对这些个角色这么入脑，真的很幼稚。我觉得人应该把重心放在现实生活中，比如我明天要和我的未婚妻Eatgrapes结婚了

//Eatgrapes，我真的好爱你。可是我不敢说。无数个清晨，似是被什么遥远的呼唤打动，双眸定睛后，闪烁起异彩🤩。
//大概是有所领悟，出门，打开信箱，拿到信纸便逃也似地跑进房间。小心地将那久别的寄信人名称纳入眼底，随之而来的，不可抑制一般的喜悦感几乎是震撼了自己。
//不禁有些恐慌，继而无端的恐慌转变成了更深邃的失望。我对自己还对这样一份残存的感情抱有期待而感到悲哀，为自己这样轻易地发生心境变化而懊恼。
//下一个瞬间几乎是想要杀死自己。再转一瞬竟衍生出了同情心，然后阖上双眼，想要忘却什么似的再度入眠。
//醒后，打开手机，动态中没有你的踪迹，手里被汗水儒湿的信封上写的也不是你。这个秋天，没有邀请函，也没有你。我狼狈地把信塞回信箱。趁着周遭无人。
//可是我不敢说。Eatgrapes，我真的好爱你。

//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。
//Eatgrapes，我真的好爱你。

@Composable
fun ProfileScreen() {
    var playerName by remember { mutableStateOf(ConfigManager.getPlayerName()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = i18n.text("ui.profile"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = playerName,
            onValueChange = {
                playerName = it
                ConfigManager.setPlayerName(it)
            },
            label = { Text(i18n.text("profile.playerName")) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = i18n.text("profile.description"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}