package tw.mymis.iot.data

import litonelevatorctrlsystem.composeapp.generated.resources.Res
import litonelevatorctrlsystem.composeapp.generated.resources.*

object DataSource {
    val flavors = listOf(
        Res.string.vanilla,
        Res.string.red_velvet,
        Res.string.chocolate,
        Res.string.salted_caramel,
        Res.string.coffee
    )

    val quantityOptions = emptyList<String>(
//        Pair(Res.string.one_cupcake, 1),
//        Pair(Res.string.six_cupcakes, 6),
//        Pair(Res.string.twelve_cupcakes, 12)
    )
}