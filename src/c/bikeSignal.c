#include <pebble.h>
#include <stdlib.h>
#include <stdio.h>
#include <math.h>

static Window *s_main_window;
static TextLayer *s_accx_layer;
static TextLayer *s_accy_layer;
static TextLayer *s_accz_layer;

static int16_t accx = 0;
static int16_t accy = 0;
static int16_t accz = 0;

static void accel_data_handler(AccelData *data, uint32_t num_samples) {
  // Read sample 0's x, y, and z values
  accx = data[0].x;
  accy = data[0].y;
  accz = data[0].z;
  /*int xlength = (int)((ceil(log10(accx))+1)*sizeof(char));
  int ylength = (int)((ceil(log10(accy))+1)*sizeof(char));
  int zlength = (int)((ceil(log10(accz))+1)*sizeof(char));
  char xtemp[xlength];
  char ytemp[ylength];
  char ztemp[zlength];
  itoa(accx, xtemp, 10);
  itoa(accy, ytemp, 10);
  itoa(accz, ztemp, 10);
  text_layer_set_text(s_accx_layer, xtemp;
  text_layer_set_text(s_accy_layer, ytemp);
  text_layer_set_text(s_accz_layer, ztemp);*/
  
  DictionaryIterator *out_iter;
  AppMessageResult result = app_message_outbox_begin(&out_iter);
  
  if(result == APP_MSG_OK) {
    // Construct the message
    dict_write_int16(out_iter, 0, accx);
    dict_write_int16(out_iter, 1, accy);
    dict_write_int16(out_iter, 2, accz);
    
    result = app_message_outbox_send();
  } else {
    // The outbox cannot be used right now
  }
}

static void main_window_load(Window *window) {
  // Get information about the Window
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  // Create the TextLayer with specific bounds
  s_accx_layer = text_layer_create(
      GRect(0, PBL_IF_ROUND_ELSE(45, 45), bounds.size.w, 50));
  s_accy_layer = text_layer_create(
      GRect(0, PBL_IF_ROUND_ELSE(60, 60), bounds.size.w, 50));
  s_accz_layer = text_layer_create(
      GRect(0, PBL_IF_ROUND_ELSE(75, 75), bounds.size.w, 50));

  // Improve the layout to be more like a watchface
  text_layer_set_background_color(s_accx_layer, GColorClear);
  text_layer_set_background_color(s_accy_layer, GColorClear);
  text_layer_set_background_color(s_accz_layer, GColorClear);
  text_layer_set_text_color(s_accx_layer, GColorBlack);
  text_layer_set_text_color(s_accy_layer, GColorBlack);
  text_layer_set_text_color(s_accz_layer, GColorBlack);
  text_layer_set_text(s_accx_layer, "AccX");
  text_layer_set_text(s_accy_layer, "AccY");
  text_layer_set_text(s_accz_layer, "AccZ");
  text_layer_set_text_alignment(s_accx_layer, GTextAlignmentCenter);
  text_layer_set_text_alignment(s_accy_layer, GTextAlignmentCenter);
  text_layer_set_text_alignment(s_accz_layer, GTextAlignmentCenter);

  // Add it as a child layer to the Window's root layer
  layer_add_child(window_layer, text_layer_get_layer(s_accx_layer));
  layer_add_child(window_layer, text_layer_get_layer(s_accy_layer));
  layer_add_child(window_layer, text_layer_get_layer(s_accz_layer));

}

static void main_window_unload(Window *window) {
  // Destroy TextLayer
  text_layer_destroy(s_accx_layer);
  text_layer_destroy(s_accy_layer);
  text_layer_destroy(s_accz_layer);
}

static void init() {
  // Create main Window element and assign to pointer
  s_main_window = window_create();

  // Set handlers to manage the elements inside the Window
  window_set_window_handlers(s_main_window, (WindowHandlers) {
    .load = main_window_load,
    .unload = main_window_unload
  });

  // Show the Window on the watch, with animated=true
  window_stack_push(s_main_window, true);
  
  app_message_open(64, 256);
}

static void deinit() {
  // Destroy Window
  window_destroy(s_main_window);
}

int main(void) {
  init();
  accel_data_service_subscribe(5, accel_data_handler);
  app_event_loop();
  deinit();
}