module.exports = function (Chart) {

  var helpers = Chart.helpers;
  helpers.extend(Chart.Controller.prototype, /** @lends Chart.Controller.prototype */ {
    initialize: function () {
      var me = this;

      // Before init plugin notification
      plugins.notify(me, 'beforeInit');

      helpers.retinaScale(me.chart);

      me.bindEvents();

      if (me.options.responsive) {
        // Initial resize before chart draws (must be silent to preserve initial animations).
        me.resize(true);
      }

      // Make sure scales have IDs and are built before we build any controllers.
      me.ensureScalesHaveIDs();
      me.buildScales();
      me.initToolTip();

      // After init plugin notification
      plugins.notify(me, 'afterInit');

      return me;
    },
    reset: function () {
      this.resetElements();
      this.tooltip.initialize();
    },
    update: function (animationDuration, lazy) {
      var me = this;

      updateConfig(me);
      Chart.plugins.notify(me, 'beforeUpdate');

      // In case the entire data object changed
      me.tooltip._data = me.data;

      // Make sure dataset controllers are updated and new controllers are reset
      var newControllers = me.buildOrUpdateControllers();

      // Make sure all dataset controllers have correct meta data counts
      helpers.each(me.data.datasets, function (dataset, datasetIndex) {
        me.getDatasetMeta(datasetIndex).controller.buildOrUpdateElements();
      }, me);

      Chart.layoutService.update(me, me.chart.width, me.chart.height);

      // Apply changes to the datasets that require the scales to have been calculated i.e BorderColor changes
      Chart.plugins.notify(me, 'afterScaleUpdate');

      // Can only reset the new controllers after the scales have been updated
      helpers.each(newControllers, function (controller) {
        controller.reset();
      });

      me.updateDatasets();

      // Do this before render so that any plugins that need final scale updates can use it
      Chart.plugins.notify(me, 'afterUpdate');

      if (me._bufferedRender) {
        me._bufferedRequest = {
          lazy: lazy,
          duration: animationDuration
        };
      } else {
        me.render(animationDuration, lazy);
      }
    },
  });
};