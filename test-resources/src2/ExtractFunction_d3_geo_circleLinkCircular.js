(function() {
  var d3_format_decimalPoint = ".", d3_format_thousandsSeparator = ",", d3_format_grouping = [ 3, 3 ];
  function d3_geo_circleClipPolygon(coordinates, context, clipLine, interpolate, angle) {
    clip.sort(d3_geo_circleClipSort);
    d3_geo_circleLinkCircular(subject);
    d3_geo_circleLinkCircular(clip);
      if (!subject.length) return;
  }
  
  function d3_geo_circleLinkCircular(array) {
    for (var i = 0, a = array[0], b, n = array.length; i < n; ) {
      a.next = b = array[++i % n];
      b.prev = a;
      a = b;
    }
  }
  function d3_geo_circleClipSort(a, b) {
    return b.angle - a.angle;
  }
})();