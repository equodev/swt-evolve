import 'dart:io';
import 'package:flutter/material.dart';
import 'package:swtflutter/src/impl/widget_config.dart';
import '../swt/tabitem.dart';
import '../impl/item_impl.dart';
import 'icons_map.dart';

class TabItemImpl<T extends TabItemSwt, V extends TabItemValue>
    extends ItemImpl<T, V> {

  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    return buildTabItemContent(context);
  }

  Widget buildTabItemContent(BuildContext context) {
    // TabItemImpl solo se encarga de la estructura del contenido
    // Los colores, negrita y estilos cuando está seleccionado se aplican desde TabFolderImpl

    return Padding(
      padding: const EdgeInsets.only(right: 2.0),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          if (state.image != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 1.0, right: 3.0),
              child: !materialIconMap.containsKey(state.image)
                  ? Image.file(
                File(state.image!),
                width: 16,
                height: 16,
              )
                  : Icon(
                getMaterialIconByName(state.image!),
                size: 16,
              ),
            ),
          Padding(
            padding: const EdgeInsets.only(bottom: 2.0),
            child: Text(
              state.text ?? "",
            ),
          ),
        ],
      ),
    );
  }

  // Función para enviar eventos de selección si se necesita
  void onTabSelected() {
    // Aquí podrías implementar la lógica para notificar cuando se selecciona la pestaña
  }
}