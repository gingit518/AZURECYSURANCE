// returns empirically calculated mode of array
function formulaModeEmpiric(...args) {
	if (args.length === 0) return 0;
	let sum = 0;
	for (let arg of args) sum += arg;
	let average = sum / args.length;

	const sorted = Array.from(args).sort((a, b) => a - b);
	const middle = Math.floor(sorted.length / 2);
	let median = 0;
	if (sorted.length % 2 === 0) {
		median = (sorted[middle - 1] + sorted[middle]) / 2;
	} else {
		median = sorted[middle];
	}
	return 3 * median - 2 * average;
}
