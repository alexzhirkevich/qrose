import androidx.compose.runtime.Composable

@Composable
actual fun Scanner() {

//    val permissionFactory = rememberPermissionsControllerFactory()
//
//    val permissionController = remember {
//        permissionFactory.createPermissionsController()
//    }
//
//    var hasCameraPermission by remember {
//        mutableStateOf(false)
//    }
//    LaunchedEffect(permissionController) {
//        permissionController.providePermission(Permission.CAMERA)
//        hasCameraPermission = permissionController
//            .getPermissionState(Permission.CAMERA) == PermissionState.Granted
//    }
//
//    if (hasCameraPermission){
//        val state = rememberBarcodeScannerState{
//            println(it)
//        }
//
//        Box {
//            BarcodeScanner(
//                modifier = Modifier.fillMaxSize(),
//                state = state
//            )
//
//            FilledTonalIconButton(
//                modifier = Modifier.align(
//                    Alignment.TopEnd
//                ),
//                onClick = {
//                    state.setFlashLightEnabled(
//                        !state.isFlashlightEnabled
//                    )
//                }
//            ){
//                Icon(
//                    imageVector = Icons.Default.Build,
//                    contentDescription = "Flashlight"
//                )
//            }
//        }
//    } else {
//        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Text("Camera permission was denied")
//        }
//    }
}