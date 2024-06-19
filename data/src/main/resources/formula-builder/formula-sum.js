// returns sum of array elements
function formulaSum(...args) {
	if (args.length===0) return 0;
	let sum = 0;
	for (let arg of args) sum += arg;
	return sum;
}
