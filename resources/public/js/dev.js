function timed(fun) {
  const t0 = performance.now()
  fun()
  return performance.now() - t0
}

function copy100(n) {
  var a = [...Array(n).keys()]
  for (var i = 0; i < 100; i++) {
    a = [...a, n + i]
  }
}

function mutate100(n) {
  var a = [...Array(n).keys()]
  for (var i = 0; i < 100; i++) {
    a.push(n + i)
  }
}

function runTest(m, k) {
  var a = []
  var x = 10
  for (var i = 0; i < k; i++) {
    a.push(Math.floor(timed(() => { mutate100(x) })))
    x = x * m
  }
  return a
}
